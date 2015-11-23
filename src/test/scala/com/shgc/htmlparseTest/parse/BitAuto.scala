package com.shgc.htmlparseTest.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

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
  val pattern = Pattern.compile("��̳")

  @Test
  def run = {
    val url = "http://baa.bitauto.com/changanv5/thread-7626972.html"
    val html = Jsoup.connect(url).get().toString
    val doc = Jsoup.parse(html)
    val title = doc.select("#TitleForumLink").text()
    val topic = doc.select("TitleTopicSt").text()
    val clickAndReplay = doc.select("[class=title_box] span").text()

    val list = doc.select(".postcontbox .postcont_list")
    val putsArray = new Array[Put](list.size)
    var i = 0
    for(t <- elements2List(list) if t.select("[span:contains(已禁用)]") != null ){

      val contArray = new Array[(String, String, String)](10)
      contArray(0) = ("comments", "username", t.select("[class=user_name]").text())
      contArray(1) = ("comments", "level", t.select("li:contains(等)").text())
      contArray(2) = ("comments", "tiezi", t.select("li:contains(帖)").text())
      contArray(3) = ("comments", "area", t.select("li:contains(地)").text())
      contArray(4) = ("comments", "car", t.select("li:contains(车)").text())
      contArray(5) = ("comments", "registerTime", t.select("li:contains(册)").text())
      contArray(6) = ("comments", "time", t.select("span[role=postTime]").text()) //�
      contArray(7) = ("comments", "comment", t.select("div[class=post_width]").text()) //����
      contArray(8) = ("comments", "floor", t.select("div[class=floor_box]").text()) //
      val host = new URL(url).getHost
      val carType = title.substring(0, title.length - 2)
      val time = getTime(contArray(6)._3)
      val key = host + " " * (20 - host.length) + "|" + carType + " " * (8-carType.length) +
        "|" + time + "|" + url + "|" + contArray(8)
      println(key)
      val put = new Put(Bytes.toBytes(key))
      val ss = contArray.filter(_ != null)
      for(arr <- contArray if arr != null){
        put.addColumn(Bytes.toBytes(arr._1), Bytes.toBytes(arr._2), Bytes.toBytes(arr._3))
        println(arr._3)
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
