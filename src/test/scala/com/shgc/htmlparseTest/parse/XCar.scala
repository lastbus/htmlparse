package com.shgc.htmlparseTest.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{TimeUtil, FloorUtil}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.Test

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/23.
 */
@Test
class XCar {



  @Test
  def run ={
    val url = "http://www.xcar.com.cn/bbs/viewthread.php?tid=21933212"
    val html = Jsoup.connect(url).get().html()
    val doc = Jsoup.parse(html)

    var temp: String = null

    val luntan = doc.select("#_img div.F_box_2 table h1 a").text()
    val problem = doc.select("#_img div.F_box_2 table h1").text().split("<")(0)
    val viewAndReplay = doc.select("#showPic span").text()
    println(luntan + "  " + problem +  "  " +  viewAndReplay)
    val lists = doc.select("#delpost .F_box_2")
    val putsArray = new Array[Put](lists.size())
    for(list <- elements2List(lists)){

      val arr = new Array[(String, String, String)](15)
      temp = list.select("td:eq(0) > a.bold").text()
      arr(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
      temp = list.select("td:eq(0) p[data]").text()
      arr(1) = if(temp != null && temp.length > 0) ("comments", "level", temp) else null
      temp = list.select("td:eq(0) div.smalltxt p:last-child").text()
      arr(2) = if(temp != null && temp.length > 0) ("comments", "all", temp) else null

      val matcher1 = caiChanPattern.matcher(temp)
      val caiChan = if(matcher1.find()) matcher1.group() else null
      if(caiChan != null) arr(9) = ("comments", "caichan", caiChan.substring(0, caiChan.indexOf("爱")).trim)

      val match2 = registerPattern.matcher(temp)
      val zhuce = if(match2.find()) match2.group() else null
      if(zhuce != null) arr(10) = ("comments", "zhuce", registerTime.format(registerTime.parse(zhuce)))

      val matcher4 = tieZiPattern.matcher(temp)
      val tieZi = if (matcher4.find()) matcher4.group() else null
      if(tieZi != null) arr(11) = ("comments", "tiezi", tieZi.substring(0, tieZi.indexOf("帖")).trim)

      val matcher5 = areaPattern.matcher(temp)
      val area = if(matcher5.find()) matcher5.group() else null
      if(area != null) arr(12) = ("comments", "area", if(area.split(":").length > 1) area.split(":")(1) else area.split("：")(1) )

      temp = list.select("td:eq(1) table tbody tr:eq(0) td a").text()
      arr(3) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null
      temp = list.select("td:eq(1) table tbody tr:eq(0) td div div:contains(发表于)").text()
      arr(4) = if(temp != null && temp.length > 0) ("comments", "post-time", TimeUtil.getPostTime(temp)) else null
      temp = list.select("td:eq(1) table tbody tr:eq(0) td a.link_bg").text()
      arr(5) = if(temp != null && temp.length > 0) ("comments", "client-side", temp) else null


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

      val carType = luntan.substring(0, luntan.length -2)
//      println(getTime(arr(4)._3))
      val time = arr(4)._3
      val key = "xcar" + " " * 4 + "|" + carType + "|" + time + "|" + url + "|" + arr(3)._3
      println(key)
      val put = new Put(Bytes.toBytes(key))
      for(a <- arr if a != null){
        put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        println(a)
      }



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

  implicit def elements2List(elements: Elements): Array[Element] ={
    val arr = new ArrayBuffer[Element]
    val e = elements.listIterator()
    while(e.hasNext)
      arr += e.next()
    arr.toArray
  }

  def getSplit(s: String): String = {
    val matcher1 = caiChanPattern.matcher(s)
    if(matcher1.find()) matcher1.group() else null
    val match2 = registerPattern.matcher(s)
    if(match2.find()) match2.group() else null
    val matcher3 = registerPattern.matcher(s)
    if(matcher3.find()) matcher3.group() else null
    val matcher4 = tieZiPattern.matcher(s)
    if (matcher4.find()) matcher4.group() else null
    val matcher5 = areaPattern.matcher(s)
    if(matcher5.find()) matcher5.group() else null
  }

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")

  val caiChanPattern = Pattern.compile("\\d+[ ]爱卡币")
  val registerPattern = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")
  val tieZiPattern = Pattern.compile("\\d+[ ]*帖")
  val areaPattern = Pattern.compile("来自[：:][ ]*.*$")

  val registerTime = new SimpleDateFormat("yyyyMMdd")

}
