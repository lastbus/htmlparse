package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{FloorUtil, TimeUtil, NumExtractUtil, Selector}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/11/24.
 */
class PCAutoParser extends Parser {


  override def run(content: Content, selector: Selector): Array[Put] = {

    val contentType = content.getMetadata.get("Content-Type").split("=")
    val encoding = if(contentType.length > 1) contentType(1) else "gb2312"
    try {
      val html = new String(content.getContent, encoding)
      val url = content.getUrl
      val doc = Jsoup.parse(html)

      var temp: String = null
      val luntan = doc.select("#content .com-crumb a:eq(6)").text()
      val view = doc.select("#views").text() // kan bu dao
      val reply = doc.select(".overView span").last().text()
      val title = doc.select(".post_right .post_r_tit .yh").text()


      val lists = doc.select("#post_list .postids")
      val puts = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](10)
        temp = list.select("a.needonline").text().trim
        arr(0) = if(temp != null && temp.length > 0)  ("comments", "username", temp) else null
        temp = list.select(".user_atten li:eq(0)").text().trim
        arr(1) = if(temp != null && temp.length > 0) ("comments", "fans", NumExtractUtil.getNumArray(temp)(0)) else null
        temp = list.select(".user_atten li:eq(1)").text().trim
        arr(2) = if(temp != null && temp.length > 0) ("comments", "jinghua", NumExtractUtil.getNumArray(temp)(0)) else null
        temp = list.select(".user_atten li:eq(2)").text().trim
        arr(3) = if(temp != null && temp.length > 0) ("comments", "publish", NumExtractUtil.getNumArray(temp)(0)) else null
        temp = list.select(".post_time").text().trim
        arr(4) = if(temp != null && temp.length > 0) ("comments", "posttime", getPostTime(temp) ) else null

        temp = list.select(".normal_msg table tbody tr:eq(0) td:eq(0) .replyBody span.cite font[color=#05A]").text().trim
        if(temp!=null && temp.length > 0){
          arr(7) = if(temp != null && temp.length > 0) ("comments", "replywho", temp.split(" ")(0)) else null
          //        temp = list.select(".normal_msg td:eq(0) .replyBody span.cite font[color=#05A] a:eq(1)").text().trim
          arr(8) = if(temp != null && temp.length > 0) ("comments", "floor2", FloorUtil.getFloorNumber(temp.split(" ")(1))) else null
        }else{
          temp = list.select(".post_msg").text().trim
          arr(5) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
        }

        temp = list.select(".post_floor em, .post_floor").text().trim
        arr(6) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null  //从 1 开始

        temp = list.select(".post_main .post_edition").text().trim
        arr(9) = if(temp != null && temp.length > 0) ("comments", "clientside", temp.substring(2)) else null


        val time = arr(4)._3
        val key = "pcauto" + " " * 2 + "|" + luntan.substring(0, luntan.length - 2) + "|" + time + "|" + url + "|" + arr(6)._3

        val put = new Put(Bytes.toBytes(key))
        if (title != null && title.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(title))
        if (view != null && view.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(view))
        if (reply != null && reply.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("reply"), Bytes.toBytes(reply))

        for (c <- arr if c != null && c._3 != null) {
          put.addColumn(Bytes.toBytes(c._1), Bytes.toBytes(c._2), Bytes.toBytes(c._3))
        }
        puts(i) = put
        i += 1
      }

      return puts
    }catch {
      case _ :Exception => return null
    }
    }

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

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val sdf3 = new SimpleDateFormat("yyyy-MM-dd")
  val sdfRegister = new SimpleDateFormat("yyyyMMdd")
  val registerTimePattern = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")

}
