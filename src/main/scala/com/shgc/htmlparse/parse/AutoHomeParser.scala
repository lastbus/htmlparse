package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{TimeUtil, NumExtractUtil, Selector}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.LogManager
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/20.
 */
class AutoHomeParser extends Parser {
  @transient val LOG = LogManager.getLogger(this.getClass.getName)
  var urlMap: Map[Pattern, Selector] = null


  //  override def run(content: Content, selector: Selector): Array[Put] = {
  //    val html = new String(content.getContent, selector.encoding)
  //    val url = content.getUrl
  //    val keys = selector.keys
  //    val doc = Jsoup.parse(html)
  //    val separator = selector.separator
  //
  //    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  //    val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  //
  //    var floor: String = null
  //    val list = doc.select(selector.body)
  //    val putsArrayBuffer = new ArrayBuffer[Put]
  //    for(element <- elements2List(list)){
  //      //将取出的值放入一个临时的 ArrayBuffer 中
  //      val values = new ArrayBuffer[(String, String, String)]
  //
  //      for((select, columnFamily, column) <- selector.select){
  //        values += ((columnFamily, column, element.select(select).text()))
  //        if(column.equalsIgnoreCase("FLOOR")) floor = formatFloor(values.last._3)
  //      }
  //
  //      //一种情况是回复别人，另一种是发言
  //      if(element.select(selector.strategySelector(0)(0)._1).text() != null) {
  //        for (sel <- selector.strategySelector(0)) {
  //          values += ((sel._2, sel._3, element.select(sel._1).text()))
  //        }
  //      }else{
  //        for(sel <- selector.strategySelector(1)){
  //          values += ((sel._2, sel._3, element.select(sel._1).text()))
  //        }
  //      }
  //      val host = new URL(url).getHost
  //      val keyBuffer = new StringBuilder(host + separator)
  //      //生成主键
  //      for(key <- keys){
  //        for(col <- values if (col._2.equalsIgnoreCase(key))){
  //          if(key.equalsIgnoreCase("TIME")){
  //            keyBuffer.append(sdf2.format(sdf.parse(col._3)) + separator)
  //          } else if(key.equalsIgnoreCase("CARTYPE")){
  //            keyBuffer.append(col._3.substring(0, col._3.length - 2) + separator)
  //          }else keyBuffer.append(col._3 + separator)
  //        }
  //      }
  //      val k = keyBuffer.append(url).append(floor)
  //      //make the Put
  //      val put = new Put(Bytes.toBytes(keyBuffer.toString()))
  //      for((columnFamily, column, value) <- values){
  //        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value))
  //      }
  //      putsArrayBuffer += put
  //    }
  //    putsArrayBuffer.toArray
  //
  //    null
  //  }


  def getCarType(text: String): String = {
    val pattern = Pattern.compile("论坛")
    val matcher = pattern.matcher(text)
    if (matcher.find()) text.slice(0, matcher.start()) else text
  }

  def formatFloor(floor: String): String = {
    if (floor.equals("楼主")) {
      "0"
    } else if (floor.equals("沙发")) {
      "1"
    } else if (floor.equals("板凳")) {
      "2"
    } else if (floor.equals("地板")) {
      "3"
    } else floor.substring(0, floor.length - 2)
  }

  override def run(content: Content, selector: Selector): Array[Put] = {
    try {
      val html = new String(content.getContent, "gb2312")
      val url = content.getUrl
      val doc = Jsoup.parse(html)

      var temp: String = null
      temp = doc.select("#a_bbsname").text().split(" ")(0)
      val carType = if(temp != null && temp.length > 2) temp.substring(0, temp.length - 2) else null
      temp = doc.select("#x-views").text()
      val click = if(temp != null  && temp.length > 0) temp else null
      temp = doc.select("#x-replys").text()
      val replay = if(temp != null  && temp.length > 0) temp else null
      //帖名
      val problem = doc.select("#consnav span")
      val topic = if(problem != null && problem.size > 0 ) problem.last().text() else null

      val body = doc.select("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F]")
      val putsArray = new Array[Put](body.size)
      var i = 0
      for (b <- elements2List(body)) {
        val arr = new Array[(String, String, String)](16)

        temp = b.select("[class=txtcenter fw]").text().trim
        arr(0) = if (temp != null && temp.length > 0) ("comments", "username", temp) else null  //username
        temp = b.select(".lv-txt").text().trim
        arr(1) = if (temp != null && temp.length > 0) ("comments", "level",temp ) else null  //level

        val jingHua = NumExtractUtil.getNumArray(b.select("li:contains(精华)").text())
        if (jingHua != null && jingHua.size == 0) arr(2) = ("comments", "jinghua", jingHua(0))  //精华
        val tieZi = NumExtractUtil.getNumArray(b.select("li:contains(帖子)").text())
        if(tieZi.size == 2 ) {
          arr(3) = ("comments", "publish", tieZi(0)) //发布帖子数
          arr(16) = ("comments", "response", tieZi(1)) //回复帖子数
        }

        temp = b.select("li:contains(注册)").text().trim
        arr(4) =  if(temp != null && temp.length > 0) ("comments", "registertime", TimeUtil.getAutoHomeRT(temp)) else null//register time
        temp = b.select("li:contains(来自)").text().trim
        arr(5) = if(temp != null && temp.length > 3) ("comments", "area", temp.substring(3)) else null
        temp = b.select("li:contains(所属)").text().trim
        arr(6) = if(temp != null && temp.length > 3) ("comments", "suoshu", temp.substring(3)) else null
        temp = b.select("li:contains(关注)").text().trim
        arr(7) = if(temp != null && temp.length > 3) ("comments", "guanzhu", temp.substring(3)) else null
        temp = b.select("li:contains(爱车)").text().trim
        arr(8) = if(temp != null && temp.length > 3) ("comments", "aiche", temp.substring(3)) else null
        temp = b.select("span[xname=date]").text().trim
        arr(9) = if(temp != null && temp.length > 6) ("comments", "posttime", TimeUtil.getFloorTime1(temp)) else null
        temp = b.select("a[class=rightbutlz fr], div[class=fr]").text().trim
        arr(10) = if(temp != null && temp.length > 3) ("comments", "floor", temp.substring(0, temp.length -1)) else null
        temp = b.select("div[class=plr26 rtopconnext] span:contains(来自) a").text.trim
        arr(11) = if(temp != null && temp.length > 0) ("comments", "clientside", temp) else null //手机客户端
        //设计评论部分
        if (b.select(".w740 .relyhfcon p a:contains(楼)") != null) {
          temp = b.select(".w740 .relyhfcon p a:contains(楼)").text().trim
          arr(12) = if(temp != null && temp.length > 0) ("comments", "floor2", temp.substring(0, temp.length - 1)) else null
          temp = b.select(".w740 .rrlycontxt").text().trim
          arr(13) = if(temp != null && temp.length > 0) ("comments", "comment2", temp) else null
          temp =  b.select(".w740 .yy_reply_cont").text().trim
          arr(14) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
        } else {
          temp = b.select(".w740").text().trim
          arr(15) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else  null
        }

        val key = "autohome"  + "|" + carType + "#" * (8 - carType.length) + "|" +
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

     return putsArray
    }catch {
      case _ :Exception => { LOG.error("exception: " + content.getUrl);return null}
    }

  }

}
