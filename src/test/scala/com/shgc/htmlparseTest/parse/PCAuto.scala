package com.shgc.htmlparseTest.parse

import java.net.URL
import java.text.SimpleDateFormat

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
class PCAuto {

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

  @Test
  def runTest = {
    val url = "http://bbs.pcauto.com.cn/topic-8240915.html"
    val html = Jsoup.connect(url).get().html()
    val doc = Jsoup.parse(html)

    val luntan = doc.select("#content .com-crumb a").text().split(" ")(3)
    val view = doc.select(".overView").text()
    val replay = doc.select(".overView span").last().text()
    val title = doc.select(".post_right .post_r_tit .yh").text()
    println(luntan + ":" + view + " : " + replay + " : " + title )
    val lists = doc.select("#post_list .postids")
    val puts = new Array[Put](lists.size())
    for(list <- elements2List(lists)){
      val arr = new Array[(String, String, String)](10)
      arr(0) = ("comments", "username", list.select("a.needonline").text())
      arr(1) = ("comments", "fans", list.select(".user_atten li:eq(0)").text())
      arr(2) = ("comments", "jinghua", list.select(".user_atten li:eq(1)").text())
      arr(3) = ("comments", "tiezi", list.select(".user_atten li:eq(2)").text())
      arr(4) = ("comments", "time", list.select(".post_time").text())
      arr(5) = ("comments", "comment", list.select(".post_msg").text())
      arr(6) = ("comments", "floor", list.select(".post_floor").text())
//      ("comments", "", list.select("").text())
//      ("comments", "", list.select("").text())

      val host = new URL(url).getHost
      val time = sdf2.format(sdf.parse(arr(4)._3.substring(3)))
      val key = host + " " * (20 - host.size) + "|" + luntan.substring(0, luntan.length - 2) + "|" + time + "|" + url + "|" + arr(6)._3
      println(key)

      val put = new Put(Bytes.toBytes(key))
      for(c <-arr if c!= null){
//        put.addColumn(Bytes.toBytes(c._1), Bytes.toBytes(c._2), Bytes.toBytes(c._3))
        println(c)
      }


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
