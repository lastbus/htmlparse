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
class SoHuParse extends Parser{

  var vehicleBandAndCarType: Map[String, String] = null
  val columnFamily = Bytes.toBytes("comments")

  override def run(content: Content, selector: Selector): Array[Put] = {
    if(vehicleBandAndCarType == null || vehicleBandAndCarType.size == 0) return null
    val  contentType = content.getMetadata.get("Content-Type").split("=")
    val encoding = if(contentType.length > 1) contentType(1) else "gbk"
    val url = content.getUrl
    val html = new String(content.getContent, encoding)
    val doc = Jsoup.parse(html)
    var temp: String = null
//    try {
      val luntan = doc.select("body .wapper980 .conmain .con-head h1 a").text().trim
      val carType = if(luntan.contains("车友会")) luntan.substring(0, luntan.indexOf("车友会")) else luntan
      val vehicle = vehicleBandAndCarType.getOrElse(carType, "unknown-sohu")
      val problem = doc.select("body .wapper980 .conmain .con-head h1").text().trim.split(" ")
      val topic = problem(problem.size - 1)

      val clickAndView = doc.select("body .wapper980 .conmain .con-head span.con-head-info").text().trim

      val lists = doc.select("body div.wapper980 div.conmain div.con-wrap[id^=floor-]")
      val putsArray = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](10)
        temp = list.select("div.con-side a.user-nickname").text().trim
        arr(0) = if(temp != null && temp.length >0) ("comments", "username", temp) else null
        temp = list.select("div.con-side p:contains(来自)").text().trim
        arr(1) = if(temp != null && temp.length >0) ("comments", "area", temp.substring(temp.indexOf("：") + 1)) else null
        temp = list.select("div.con-side p:contains(等级)").text().trim
        arr(2) = if(temp != null && temp.length >0) ("comments", "level",  temp.substring(temp.indexOf("：") + 1)) else null
        temp = list.select("div.con-side p:contains(注册)").text().trim
        arr(3) = if(temp != null && temp.length >0) ("comments", "registertime", getRegisterTime(temp)) else null
        temp = list.select("div.con-side p:contains(爱车)").text().trim
        arr(4) = if(temp != null && temp.length >0) ("comments", "aiche", temp) else null
        temp = list.select("div.con-main-wapper span.floor-time").text().trim
        arr(5) = if(temp != null && temp.length >0) ("comments", "posttime", getPostTime(temp)) else null
        temp = if(i ==0) "楼主" else list.select("div.con-main-wapper span.floor").text().trim
        arr(6) = if(temp != null && temp.length >0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else  null

        //1正常发言  2 回复上面楼层
        temp = list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]").text().trim
        if (temp.length < 1) {
          temp = list.select("div.con-main-wapper div.con-main div.main-bd").text().trim
          arr(7) = if(temp != null && temp.length >0) ("comments", "comment", temp) else null
        } else {
          arr(8) = ("comments", "replywho", temp.split(" ")(0))
          val temp2 = list.select("div.con-main-wapper div.con-main div.main-bd").text().trim
//          println(temp2.substring(temp.length))
          arr(9) = ("comments", "comment", temp2.substring(temp.length))
        }

        val time = arr(5)._3

        val key = "sohu" + "|" + vehicle + "|" + carType + "|" + time +
          "|" + url + "|" + arr(6)._3

        val put = new Put(Bytes.toBytes(key))
        if (topic != null && topic.length > 0) {
          put.addColumn(columnFamily,
            Bytes.toBytes("topic"), Bytes.toBytes(topic))
        }
        if (clickAndView != null && clickAndView.length > 0) {
          val clickView = NumExtractUtil.getNumArray(clickAndView)
          if(clickAndView != null && clickAndView.length == 2) {
            put.addColumn(columnFamily, Bytes.toBytes("reply"), Bytes.toBytes(clickView(0)))
            put.addColumn(columnFamily, Bytes.toBytes("view"), Bytes.toBytes(clickView(1)))
          }
        }

        put.addColumn(columnFamily, Bytes.toBytes("chexing"), Bytes.toBytes(carType))
        put.addColumn(columnFamily, Bytes.toBytes("pinpai"), Bytes.toBytes(vehicle))

        for (a <- arr if a != null && a._3.length > 0) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
//    }catch {
//      case _ : Exception => return  null
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

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val sdf3 = new SimpleDateFormat("yyyy-MM-dd")
  val sdfRegister = new SimpleDateFormat("yyyyMMdd")
  val registerTimePattern = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")


  /*
  错误 记录
  1 数组下标溢出了： val carType = luntan.substring(0, luntan.length - 3)
   */
}
