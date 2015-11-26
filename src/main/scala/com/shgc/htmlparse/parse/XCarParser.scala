package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{NumExtractUtil, TimeUtil, FloorUtil, Selector}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/11/24.
 */
class XCarParser extends Parser {

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")

  override def run(content: Content, selector: Selector): Array[Put] = {
    try{
    val html = new String(content.getContent, "gb2312")
    val url = content.getUrl
    val baseUrl = content.getBaseUrl
    val host = new URL(url).getHost
    val doc = Jsoup.parse(html)
    var temp: String = null

    val luntan = doc.select("#_img div.F_box_2 table h1 a").text()
    val problem = doc.select("#_img div.F_box_2 table h1").text().split("<")(0)
    val viewAndReplay = NumExtractUtil.getNumArray(doc.select("#showPic span").text())

    val lists = doc.select("#delpost .F_box_2")
    val putsArray = new Array[Put](lists.size())
    var i = 0
    for (list <- elements2List(lists)) {

      val arr = new Array[(String, String, String)](9)
      temp = list.select("td:eq(0) > a.bold").text()
      arr(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
      temp = list.select("td:eq(0) p[data]").text()
      arr(1) = if(temp != null && temp.length > 0) ("comments", "level", temp) else null
      temp = list.select("td:eq(0) div.smalltxt p:last-child").text()
      arr(2) = if(temp != null && temp.length > 0) ("comments", "all", temp) else null
      temp = list.select("td:eq(1) table tbody tr:eq(0) td a").text()
      arr(3) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null
      temp = list.select("td:eq(1) table tbody tr:eq(0) td div div:contains(发表于)").text()
      arr(4) = if(temp != null && temp.length > 0) ("comments", "posttime", TimeUtil.getPostTime(temp)) else null
      temp = list.select("td:eq(1) table tbody tr:eq(0) td a.link_bg").text()
      arr(5) = if(temp != null && temp.length > 0) ("comments", "clientside", temp) else null


      temp = list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1 div.msgbody").text().trim
      if(temp != null && temp.length < 1){
        //正常回复
        temp = list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1").text().trim
        arr(6) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
      }else{
        //有引用别人回复
        val temp2 = list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1 div.msgbody div.msgheader a").text().trim
        arr(7) = if(temp != null && temp.length > 0) ("comments", "replywho", temp2) else null
        val length = temp.length
        temp = list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1").text().trim
        arr(8) = if(temp != null && temp.length > 0) ("comments", "comment", temp.substring(length)) else null
      }

      val carType = luntan.substring(0, luntan.length - 2)
      val time = arr(4)._3
      val key = "xcar" + " " * 4 + "|" + carType + "#" * (8 - carType.length) + "|" + time + "|" + url + "|" + arr(3)._3
      val put = new Put(Bytes.toBytes(key))
      if(problem != null) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(problem))
      if(viewAndReplay != null && viewAndReplay.length == 2){
        put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("reply"), Bytes.toBytes(viewAndReplay(0)))
        put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(viewAndReplay(1)))
      }
      for (a <- arr if a != null && a._3.length >0) {
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


  def getTime(s: String): String ={
    val matcher = timePattern.matcher(s)
    var time: String = null
    if(matcher.find()){
      time = s.substring(matcher.start(), matcher.end())
    }
    time
  }

}