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
 * Created by Administrator on 2015/11/24.
 */
@Test
class XinLangAuto {

  @Test
  def run = {
    val url = "http://bbs.auto.sina.com.cn/45/thread-5064701-1-1.html"
    val  html = Jsoup.connect(url).get().html()
    val  doc = Jsoup.parse(html)

    val luntan = doc.select("#wrap span a[name=D]").text()
    println(luntan)
    val problem = doc.select("form h1").text()
    println(problem)
    val view = doc.select("form table tbody tr:eq(0) td:eq(1) div.postmessage div:contains(被浏览) span:not(#view_count)").text()
    println("view   " + doc.select("#view_count").text())
    println("replay  " + view)

    val lists = doc.select("form .mainbox")
    val putsArray = new Array[Put](lists.size())
    var i = 0
    for(list <- elements2List(lists)){
      val arr = new Array[(String,String, String)](9)
      arr(0) = ("comments", "username", list.select("table tbody tr:eq(0) td:eq(0) cite a").text())
//      arr(1) = ("comments", "tie-zi", list.select("table tbody tr:eq(0) td:eq(0) dl dd:eq(0)").text())
//      arr(2) = ("comments", "jing-hua", list.select("table tbody tr:eq(0) td:eq(0) dl dd:eq(1)").text())
//      arr(3) = ("comments", "li-cheng", list.select("table tbody tr:eq(0) td:eq(0) dl dd:eq(2)").text())
//      arr(4) = ("comments", "xin-lang-bi", list.select("table tbody tr:eq(0) td:eq(0) dl dd:eq(3)").text())
      arr(5) = ("comments", "floor", list.select("table tbody tr:eq(0) td:eq(1) div.postinfo strong").text())
      arr(6) = ("comments", "time", list.select("table tbody tr:eq(0) td:eq(1) div.postinfo").text())
      arr(7) = ("comments", "comment", list.select("table tbody tr:eq(0) td:eq(1) div.postmessage div.t_msgfont").text())
      arr(8) = ("comments", "label", list.select("table tbody tr:eq(0) td:eq(0) p em").text())


      val host = new URL(url).getHost
      val carType = luntan.substring(0, luntan.length -2)
      val time = sdf2.format(sdf.parse(getTime(arr(6)._3)))
      val key = host + " " * (20 - host.length) + "|" + carType + "|" + time + "|" + url +"|" + arr(5)._3
      println(key)
      val put = new Put(Bytes.toBytes(key))
      for(a <- arr if a != null && a._3 != null){
        println(a)
        put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
      }

      putsArray(i) = put
      i += 1


    }




  }


  implicit def elements2List(elements: Elements): Array[Element] ={
    val arr = new ArrayBuffer[Element]
    val e = elements.listIterator()
    while(e.hasNext)
      arr += e.next()
    arr.toArray
  }

  def getTime(s: String): String ={
    val matcher = timePattern.matcher(s)
    var time: String = null
    if(matcher.find()){
      time = s.substring(matcher.start(), matcher.end())
    }
    time
  }

  def getView(s: String): String ={
    val matcher = viewPattern.matcher(s)
    var view: String = null
    if(matcher.find()){
      println("yes")
      view = s.substring(matcher.start(), matcher.end())
      println(matcher.start())
      println(matcher.end())
    }
    view
  }

  def getRepaly(s : String) : String ={
    val matcher = replayPattern.matcher(s)
    var replay: String = null
    if(matcher.find()){
      println("yes")
      replay = s.substring(matcher.start(), matcher.end())
    }
    replay
  }

  val timePattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}")
  val viewPattern = Pattern.compile("浏览 \\.*次")
  val replayPattern = Pattern.compile("回复\\.{1,}次")
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

}
