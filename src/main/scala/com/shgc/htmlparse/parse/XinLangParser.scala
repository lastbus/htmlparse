package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{TimeUtil, FloorUtil, Selector}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/11/24.
 */
class XinLangParser extends Parser{

  override def run(content: Content, selector: Selector): Array[Put] = {
//    try{
    val contentType = content.getMetadata.get("Content-Type").split("=")
    val encoding = if(contentType.length > 1) contentType(1) else "gb2312"
    val html = new String(content.getContent, encoding)
    val url = content.getUrl
    val doc = Jsoup.parse(html)
    var temp: String = null
    val luntan = doc.select("#wrap span a[name=D]").text()
    val problem = doc.select("form h1").text()
    val replay = doc.select("form table tbody tr:eq(0) td:eq(1) div.postmessage div:contains(被浏览) span:not(#view_count)").text()
    val view = doc.select("#view_count").text()

    val lists = doc.select("form .mainbox")
    val putsArray = new Array[Put](lists.size())
    var i = 0
    for(list <- elements2List(lists)){
      val arr = new Array[(String,String, String)](5)
      temp = list.select("table tbody tr:eq(0) td:eq(0) cite a").text().trim
      arr(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(1) div.postinfo strong").text().trim
      arr(1) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null
      temp = list.select("table tbody tr:eq(0) td:eq(1) div.postinfo").text()
      arr(2) = if(temp != null && temp.length > 0) ("comments", "post-time", getPostTime(temp)) else null
      temp = list.select("table tbody tr:eq(0) td:eq(1) div.postmessage div.t_msgfont").text()
      arr(3) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(0) p em").text()
      arr(4) = if(temp != null && temp.length > 0) ("comments", "label", temp) else null

      val carType = luntan.substring(0, luntan.length -2)
      val time = arr(2)._3
      val key = "xinlang" + " " + "|" + carType + "|" + time + "|" + url +"|" + arr(1)._3

      val put = new Put(Bytes.toBytes(key))
      if(problem != null && problem.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(problem))
      if(view != null && view.length >0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(view))
      if(replay != null && replay.length >0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(replay))

      for(a <- arr if a != null && a._3 != null){
        put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
      }
      putsArray(i) = put
      i += 1
    }
    return putsArray
//    }catch {
//      case _ :Exception => return null
//    }
  }

  def getPostTime(s: String): String ={
    val timeString = TimeUtil.extractTimeString(s)
    if(timeString != null && timeString.length >= 6) sdf2.format(sdf.parse(timeString)) else null
  }

  def getRegisterTime(s: String): String ={
    if(s == null || s.length < 6) return null
    val matcher = registerTimePattern.matcher(s)
    if(matcher.find()) sdfRegister.format(sdf3.parse(matcher.group())) else null
  }

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val sdf3 = new SimpleDateFormat("yyyy-MM-dd")
  val sdfRegister = new SimpleDateFormat("yyyyMMdd")
  val registerTimePattern = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")

}
