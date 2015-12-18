package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{FloorUtil, TimeUtil, NumExtractUtil, Selector}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.LogManager
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.mutable.ArrayBuffer

/**
 * Created by make on 2015/11/20.
 */
class AutoHomeParser extends Parser {
  @transient val LOG = LogManager.getLogger(this.getClass.getName)
  var urlMap: Map[Pattern, Selector] = null
  var vehicleBandMap: Map[String, String] = null

  override def run(content: Content, selector: Selector): Array[Put] = {
    if(vehicleBandMap == null || vehicleBandMap.size == 0) return null
      val metaData = content.getMetadata.get("Content-Type").split("=")
      val encoding = if(metaData.length > 1) metaData(1) else "gb2312"  // 得到网页的编码
      val html = new String(content.getContent, encoding)
      val url = content.getUrl
      val doc = Jsoup.parse(html)

      var temp: String = null
      temp = doc.select("#a_bbsname").text().trim
      val carType = if(temp.contains("论坛")) temp.substring(0, temp.indexOf("论坛")).trim else null
      val vehicleBand = vehicleBandMap.getOrElse(carType, "unknown")
      temp = doc.select("#x-views").text()
      val click = if(temp != null  && temp.length > 0) temp else null
      temp = doc.select("#x-replys").text()
      val replay = if(temp != null  && temp.length > 0) temp else null
      //帖名
      val problem = doc.select("#consnav span")
      val topic = if(problem != null && problem.size > 0 ) problem.last().text() else null
      val body = doc.select("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F]")
      val putsArray = new Array[Put](body.size)
    //    try {
      var i = 0
      for (b <- elements2List(body)) {
        val arr = new Array[(String, String, String)](17)
        temp = b.select("[class=txtcenter fw]").text().trim
        arr(0) = if (temp != null && temp.length > 0) ("comments", "username", temp) else null  //username
        temp = b.select(".lv-txt").text().trim
        arr(1) = if (temp != null && temp.length > 0) ("comments", "level",temp ) else null  //level
        //版主、论坛管委会成员，这些等级比较特殊，等级取不到就取这些称号
        if(arr(1) == null){
          temp = b.select(".txtcenter:last-child").text().trim
          arr(1) = if (temp != null && temp.length > 0) ("comments", "level",temp ) else null
        }

        val jingHua = NumExtractUtil.getNumArray(b.select("li:contains(精华)").text())
        if (jingHua != null && jingHua.size > 0) arr(2) = ("comments", "jinghua", jingHua(0))  //精华
        val tieZi = NumExtractUtil.getNumArray(b.select("li:contains(帖子)").text())
        if(tieZi.size == 2 ) {
          arr(3) = ("comments", "publish", tieZi(0)) //发布帖子数
          arr(16) = ("comments", "response", tieZi(1)) //回复帖子数
        }

        temp = b.select("li:contains(注册)").text().trim
        arr(4) =  if(temp != null && temp.length > 0) ("comments", "registertime", getRegisterTime(temp)) else null  //register time
        temp = b.select("li:contains(来自)").text().trim
        arr(5) = if(temp != null && temp.length > 3) ("comments", "area", temp.substring(3)) else null
        temp = b.select("li:contains(所属)").text().trim
        arr(6) = if(temp != null && temp.length > 3) ("comments", "suoshu", temp.substring(3)) else null
        temp = b.select("li:matchesOwn(关注)").text().trim
        arr(7) = if(temp != null && temp.length > 3) ("comments", "guanzhu", temp.substring(3)) else null
        temp = b.select("li:contains(爱车)").text().trim
        arr(8) = if(temp != null && temp.length > 3) ("comments", "aiche", temp.substring(3).split(" ")(0)) else null
        temp = b.select("span[xname=date]").text().trim
//        println("temp: " + temp)
        arr(9) = if(temp != null && temp.length > 0) ("comments", "posttime", getPostTime(temp)) else null
        temp = b.select("a[class=rightbutlz fr], div[class=fr]").text().trim
        arr(10) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else null
        temp = b.select("div[class=plr26 rtopconnext] span:contains(来自) a").text.trim
        arr(11) = if(temp != null && temp.length > 0) ("comments", "clientside", temp) else null //手机客户端
        //设计评论部分
        temp = b.select(".w740 .relyhfcon p a:contains(楼)").text().trim
        if ( temp != null && temp.length > 0) {
          //回复楼上
          arr(12) = ("comments", "floor2", FloorUtil.getFloorNumber(temp))
          temp = b.select(".w740 .relyhfcon p:eq(0) a:eq(0)").text().trim
          arr(13) = if(temp != null && temp.length > 0) ("comments", "replywho", temp) else null
          temp =  b.select(".w740 .yy_reply_cont").text().trim
          arr(14) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
        } else {
          temp = b.select(".w740").text().trim
          arr(15) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else  null
          if(arr(15) == null){
            temp = b.select(".rconten").text().trim
            arr(15) = if(temp != null && temp.length > 0 && temp.indexOf("发表于") == -1) ("comments", "comment", temp) else  null
          }
        }
        if(arr(10) != null && arr(10)._3 != null){
          val key = "autohome"  + "|" + vehicleBand + "|" + carType + "|" +
            arr(9)._3 + "|" + url + "|" + arr(10)._3

          val put = new Put((Bytes.toBytes(key)))

          if (click != null && click.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(click))
          if (replay != null && replay.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(replay))
          if(topic != null  && topic.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(topic))

          for (a <- arr if a != null && a._3 != null) {
            put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
          }

          putsArray(i) = put
          i += 1
        }

      }

     return putsArray
//    }catch {
//      case _ :Exception => return null
//    }

  }

  /*
   1 加上 LOG.error("exception: " + content.getUrl) 报空指针错误 NullPointException
   2 加上数字转换以后总是报错！！！不知道什么原因。处理方法：1 将所有处理数字的步骤去掉，看看输出结果咋样
    哦，突然想起来了：是不是因为我的 TimeUtil没有继承 Serializer类的原因，传输过程中序列化编码出现问题了，ok，立刻验证一下
    验证过了，貌似还不行。
    3 问题应该出现在提取的  文本那里吧，继续验证，但是及时提取文本出错的话，正则表达式提取不到时间，那返回值也是null，怎么会出错呢？？？？
    可能是我的逻辑出错了吧
    4 终于找到原因了： 线程安全的问题！！！
   */


  def getFloorTime(s: String): String = {
    null
  }

  def getPostTime(s: String): String = {
    val timeString = TimeUtil.extractTimeString(s)
    if(timeString != null && timeString.length >= 8) sdf2.format(sdf.parse(timeString)) else null
  }

  def getRegisterTime(s: String): String ={
    if(s == null || s.length < 6) return null
    val matcher = registerTimePattern.matcher(s)
    if(matcher.find()) sdfRegister.format(sdf3.parse(matcher.group())) else null
  }

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val sdf3 = new SimpleDateFormat("yyyy年MM月dd日")
  val sdfRegister = new SimpleDateFormat("yyyyMMdd")
  val registerTimePattern = Pattern.compile("\\d{2,4}年\\d{1,2}月\\d{1,2}日")


}
