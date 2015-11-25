package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
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
      val baseUrl = content.getBaseUrl
      val host = new URL(url).getHost
      val doc = Jsoup.parse(html)
      val bbsName = doc.select("#a_bbsname").text()
      val matcher = Pattern.compile("论坛").matcher(bbsName)
      var carType: String = null
      if (matcher.find()) {
        carType = bbsName.slice(0, matcher.start())
      }

      val click = doc.select("#x-views").text()
      val replay = doc.select("#x-replys").text()
      //    val problem = doc.select("#consnav span").last().text()

      if (click == null) return null
      val body = doc.select("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F]")
      val putsArray = new Array[Put](body.size)
      var i = 0
      for (b <- elements2List(body)) {
        val arr = new Array[(String, String, String)](15)
        arr(0) = ("comments", "username", b.select("[class=txtcenter fw]").text()) //name
        arr(1) = ("comments", "level", b.select(".lv-txt").text()) //level

        arr(2) = ("comments", "jing-hua", b.select("li:contains(精华)").text()) //精华
        arr(3) = ("comments", "tie-zi", b.select("li:contains(帖子)").text()) //帖子
        arr(4) = ("comments", "register-time", b.select("li:contains(注册)").text()) //register time
        arr(5) = ("comments", "area", b.select("li:contains(来自)").text()) //area
        arr(6) = ("comments", "suo-shu", b.select("li:contains(所属)").text()) //
        arr(7) = ("comments", "guan-zhu", b.select("li:contains(关注)").text().trim) //
        arr(8) = ("comments", "ai-che", b.select("li:contains(爱车)").text().trim) //
        arr(9) = ("comments", "time", b.select("span[xname=date]").text().trim) // 发表时间
        arr(10) = ("comments", "floor", b.select("a[class=rightbutlz fr], div[class=fr]").text())
        arr(11) = ("comments", "ke-hu-duan", b.select("div[class=plr26 rtopconnext] span:contains(来自) a").text) //手机客户端

        //设计评论部分
        if (b.select(".w740 .relyhfcon p a:contains(楼)") != null) {
          arr(12) = ("comments", "floor2", b.select(".w740 .relyhfcon p a:contains(楼)").text())
          arr(13) = ("comments", "comment2", b.select(".w740 .rrlycontxt").text())
          arr(14) = ("comments", "comment", b.select(".w740 .yy_reply_cont").text())
        } else {
          arr(15) = ("comments", "comment", b.select(".w740").text())
        }
        val key = host + " " * (20 - host.length) + "|" + carType + "空" * (8 - carType.length) + "|" +
          sdf2.format(sdf.parse(arr(9)._3)) + "|" + url + "|" + arr(10)._3

        val put = new Put((Bytes.toBytes(key)))
        put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(click))
        put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(replay))
        //      put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("problem"), Bytes.toBytes(problem))
        for (a <- arr if a != null && a._3 != null) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        }

        putsArray(i) = put
        i += 1
      }

     return putsArray
    }catch {
      case _ :Exception => return null
    }
  }

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

}
