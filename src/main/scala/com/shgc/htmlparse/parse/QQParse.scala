package com.shgc.htmlparse.parse

import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{NumExtractUtil, FloorUtil, TimeUtil, Selector}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/11/25.
 */
class QQParse extends Parser{


  override def run(content: Content, selector: Selector): Array[Put] = {

    val contentType = content.getMetadata.get("Content-Type").split("=")
    val encoding = if(contentType.length > 1) contentType(1) else "utf-8"
    val url = content.getUrl
    val html = new String(content.getContent, encoding)
    val doc = Jsoup.parse(html)
    var temp: String = null

//    try {
      val luntan = doc.select("body #wp #pt div.z a:eq(8)").text().trim
      val problem = doc.select("body #wp #pt div.z a:last-child").text().trim
      val clickAndView = doc.select("#postlist table:first-child td:eq(0) span.xi1").text().trim

      val lists = doc.select("#postlist > div[id^=post_]")
      val putsArray = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](11)
        temp = list.select("table tbody tr:eq(0) td:eq(0) div.pi div.authi a.xw1").text().trim
        arr(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) table th:eq(0) a").text().trim

        arr(1) = if(temp != null && temp.length > 0) ("comments", "publish", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) table th:eq(1) a").text().trim
        arr(2) = if(temp != null && temp.length > 0) ("comments", "friends", temp) else null

//        temp = list.select("table tbody tr:eq(0) td:eq(0) table td a").text().trim  重复，去掉
//        arr(3) = if(temp != null && temp.length > 0) ("comments", "jifen", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) > div > p em a").text().trim
        arr(4) = if(temp != null && temp.length > 0) ("comments", "level", temp) else null

        temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(1)").text().trim
        arr(5) = if(temp != null && temp.length > 0) ("comments", "virtualmoeny", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(3)").text().trim
        arr(6) = if(temp != null && temp.length > 0) ("comments", "ji-fen", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(5)").text().trim
        arr(7) = if(temp != null && temp.length > 0) ("comments", "lastlogin", getRegisterTime(temp)) else null

        temp = list.select("table tbody tr:eq(0) td:eq(1) div.pti div.authi em").text().trim
        arr(8) = if(temp != null && temp.length > 0) ("comments", "posttime", getPostTime(temp)) else null
        temp = list.select("table tbody tr:eq(0) td:eq(1) div.pi > strong em").text().trim
        arr(9) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null
        temp = list.select("table tbody tr:eq(0) td:eq(1) div.pct table").text().trim
        arr(10) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null

        val key = "tencent" + " " + "|" + luntan + "#" * (8 - luntan.length) + "|" + arr(8)._3 +
          "|" + url + "|" + arr(9)._3
        val put = new Put(Bytes.toBytes(key))
        if (problem != null && problem.length > 0)
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(problem))
        if (clickAndView != null && clickAndView.length > 0) {
          val clickView = NumExtractUtil.getNumArray(clickAndView)
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(clickView(1).substring(1)))
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(clickView(0).substring(1)))
        }
        for (a <- arr if a != null) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
//    }catch {
//      case _ : Exception => return null
//    }
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
  val sdf3 = new SimpleDateFormat("yyyy-MM-dd")
  val sdfRegister = new SimpleDateFormat("yyyyMMdd")
  val registerTimePattern = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")

}
