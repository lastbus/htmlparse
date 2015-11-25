package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
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

    val luntan = doc.select("#_img div.F_box_2 table h1 a").text()
    val problem = doc.select("#_img div.F_box_2 table h1").text().split("<")(0)
    val viewAndReplay = doc.select("#showPic span").text()

    val lists = doc.select("#delpost .F_box_2")
    val putsArray = new Array[Put](lists.size())
    var i = 0
    for (list <- elements2List(lists)) {

      val arr = new Array[(String, String, String)](9)
      arr(0) = ("comments", "username", list.select("td:eq(0) > a.bold").text())
      arr(1) = ("comments", "level", list.select("td:eq(0) p[data]").text())
      arr(2) = ("comments", "all", list.select("td:eq(0) div.smalltxt p:last-child").text())
      arr(3) = ("comments", "floor", list.select("td:eq(1) table tbody tr:eq(0) td a").text())
      arr(4) = ("comments", "time", list.select("td:eq(1) table tbody tr:eq(0) td div div:contains(发表于)").text())
      arr(5) = ("comments", "ke-hu-duan", list.select("td:eq(1) table tbody tr:eq(1) td span.t_title1").text())
      //正常回复
      arr(6) = ("comments", "comment", list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1").text())

      //有引用别人回复
      arr(7) = ("comments", "huifuwho", list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1 div.msgbody div.msgheader a").text())
      arr(8) = ("comments", "shi-jian", list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1 div.msgheader").text())
      //      arr(9) = ("comments"

      val host = new URL(url).getHost
      val carType = luntan.substring(0, luntan.length - 2)
      //      println(getTime(arr(4)._3))
      val time = sdf2.format(sdf.parse(getTime(arr(4)._3)))
      val key = host + " " * (20 - host.length) + "|" + carType + "|" + time + "|" + url + "|" + arr(3)._3


      val put = new Put(Bytes.toBytes(key))
      if(problem != null) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(problem))
      if(viewAndReplay != null)put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("viewAndReplay"), Bytes.toBytes(viewAndReplay))

      for (a <- arr if a != null) {
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