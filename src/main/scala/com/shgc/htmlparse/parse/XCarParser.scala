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

  var vehicleBandAndCarType: Map[String, String] = null
  val columnFamily = Bytes.toBytes("comments")

  override def run(content: Content, selector: Selector): Array[Put] = {
    if(vehicleBandAndCarType == null || vehicleBandAndCarType.size == 0) return null

//    try{
    val contentType = content.getMetadata.get("Content-Type").split("=")
    val encoding = if(contentType.length > 1) contentType(1) else "gb2312"
    val html = new String(content.getContent, encoding)
    val url = content.getUrl
    val doc = Jsoup.parse(html)
    var temp: String = null

    val luntan = doc.select("#_img div.F_box_2 table h1 a").text()
    val carType = if(luntan.contains("论坛")) luntan.substring(0, luntan.indexOf("论坛")) else luntan
    val vehicleBand = vehicleBandAndCarType.getOrElse(carType, "unknown-xcar")
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

      val matcher1 = caiChanPattern.matcher(temp)
      val caiChan = if(matcher1.find()) matcher1.group() else null
      if(caiChan != null) arr(9) = ("comments", "jifen", caiChan.substring(0, caiChan.indexOf("爱")).trim)

      val match2 = registerPattern.matcher(temp)
      val zhuce = if(match2.find()) match2.group() else null
      if(zhuce != null) arr(10) = ("comments", "registertime", sdfRegister.format(sdfRegister.parse(zhuce)))

      val matcher4 = tieZiPattern.matcher(temp)
      val tieZi = if (matcher4.find()) matcher4.group() else null
      if(tieZi != null) arr(11) = ("comments", "publish", tieZi.substring(0, tieZi.indexOf("帖")).trim)

      val matcher5 = areaPattern.matcher(temp)
      val area = if(matcher5.find()) matcher5.group() else null
      if(area != null) arr(12) = ("comments", "area", if(area.split(":").length > 1) area.split(":")(1) else area.split("：")(1) )


      temp = list.select("td:eq(1) table tbody tr:eq(0) td a").text()
      arr(3) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null
      temp = list.select("td:eq(1) table tbody tr:eq(0) td div div:contains(发表于)").text()
      arr(4) = if(temp != null && temp.length > 0) ("comments", "posttime", getPostTime(temp)) else null
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

      val time = arr(4)._3
      val key = "xcar" + "|" + vehicleBand + "|" + carType + "|" + time + "|" + url + "|" + arr(3)._3
      val put = new Put(Bytes.toBytes(key))
      if(problem != null) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(problem))
      if(viewAndReplay != null && viewAndReplay.length == 2){
        put.addColumn(columnFamily, Bytes.toBytes("reply"), Bytes.toBytes(viewAndReplay(0)))
        put.addColumn(columnFamily, Bytes.toBytes("click"), Bytes.toBytes(viewAndReplay(1)))
      }
      put.addColumn(columnFamily, Bytes.toBytes("chexing"), Bytes.toBytes(carType))
      put.addColumn(columnFamily, Bytes.toBytes("pinpai"), Bytes.toBytes(vehicleBand))
      for (a <- arr if a != null && a._3.length >0) {
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

  val caiChanPattern = Pattern.compile("\\d+[ ]爱卡币")
  val registerPattern = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")
  val tieZiPattern = Pattern.compile("\\d+[ ]*帖")
  val areaPattern = Pattern.compile("来自[：:][ ]*.*$")

}