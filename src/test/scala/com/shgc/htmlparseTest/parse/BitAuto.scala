package com.shgc.htmlparseTest.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{FloorUtil, TimeUtil, NumExtractUtil}
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
class BitAuto {

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val pattern = Pattern.compile("")

  @Test
  def run = {
    val url = "http://baa.bitauto.com/changanv5/thread-7626972.html"
    val html = Jsoup.connect(url).get().toString
    val doc = Jsoup.parse(html)
    var temp: String = null
    val title = doc.select("#TitleForumLink").text()
    val topic = doc.select("#TitleTopicSt").text()
    println(topic)
    val clickAndReplay = doc.select("[class=title_box] span").text().trim
    println(clickAndReplay)
    val list = doc.select(".postcontbox .postcont_list")
    val putsArray = new Array[Put](list.size)
    var i = 0
    for(t <- elements2List(list) if t.select("[span:contains(已禁用)]") != null ){

      val contArray = new Array[(String, String, String)](15)
      temp = t.select("[class=user_name]").text().trim
      contArray(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
      temp = t.select("li:contains(等)").text().trim
      contArray(1) = if(temp != null && temp.contains("：")) ("comments", "level", temp.substring(temp.indexOf("：") + 1).trim) else null
      temp = t.select("li:contains(帖)").text().trim
      if(temp != null && temp.contains("精华")){
        val result = NumExtractUtil.getNumArray(temp)
        if(result.size == 2){
          contArray(2) =  ("comments", "tiezi", result(0))
          contArray(3) = ("comments", "jing-hua", result(1))
        }
      }
      temp = t.select("li:contains(地)").text().trim
      contArray(9) = if(temp != null && temp.contains("：")) ("comments", "area",temp.substring(temp.indexOf("：") + 1).trim) else null
      temp = t.select("li:contains(车)").text().trim
      contArray(4) = if(temp != null && temp.contains("：")) ("comments", "car", temp.substring(temp.indexOf("：") + 1).trim) else null
      temp = t.select("li:contains(册)").text().trim
      contArray(5) = if(temp != null && temp.length > 0) ("comments", "registerTime", TimeUtil.getBitAutoTime(temp)) else null
      temp = t.select("span[role=postTime]").text().trim
      contArray(6) = if(temp != null && temp.length > 0) ("comments", "post-time", TimeUtil.getPostTime(temp)) else null
      temp = t.select("div[class=post_width]").text().trim
      contArray(7) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
      temp = t.select("div[class=floor_box]").text().trim
      contArray(8) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else null

      val carType = title.substring(0, title.length - 2)
      val time = contArray(6)._3
      val key = "bitauto" + " " + "|" + carType + "#" * (8-carType.length) +
        "|" + time + "|" + url + "|" + contArray(8)._3
      println(key)

      val put = new Put(Bytes.toBytes(key))
      if(clickAndReplay != null) {
        val t = NumExtractUtil.getNumArray(clickAndReplay)
        put.addColumn(Bytes.toBytes("comment"), Bytes.toBytes("replay"), Bytes.toBytes(t(0)))
        put.addColumn(Bytes.toBytes("comment"), Bytes.toBytes("view"), Bytes.toBytes(t(1)))
      }
      for(arr <- contArray if arr != null){
        put.addColumn(Bytes.toBytes(arr._1), Bytes.toBytes(arr._2), Bytes.toBytes(arr._3))
        println(arr)
      }
      putsArray(i)= put
      i += 1
    }

  }

  def getTime(timeString: String): String ={
    try{
      sdf2.format(sdf.parse(timeString))
    }catch {
      case _ : Exception  => sdf2.format(sdf.parse(timeString.substring(3)))
    }
  }

  implicit def elements2List(elements: Elements): Array[Element] ={
    val arr = new ArrayBuffer[Element]
    val e = elements.listIterator()
    while(e.hasNext)
      arr += e.next()
    arr.toArray
  }
}
