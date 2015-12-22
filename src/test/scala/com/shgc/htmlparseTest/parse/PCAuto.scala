package com.shgc.htmlparseTest.parse

import java.net.URL
import java.text.SimpleDateFormat

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
class PCAuto {

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

  @Test
  def runTest = {
    val url = "http://bbs.pcauto.com.cn/topic-9445409.html"
    val html = Jsoup.connect(url).get().html()
    val doc = Jsoup.parse(html)

    var temp: String = null
    val luntan = doc.select("#content .com-subHead .com-crumb a:eq(6)").text()
    val vehicle = doc.select("#content .com-subHead .com-crumb a:eq(4)").text()
    val view = doc.select("#views").text()
    val replay = doc.select(".overView span").last().text()
    val title = doc.select(".post_right .post_r_tit .yh").text()
    println(vehicle + " ==  " + luntan + ":" + view + " : " + replay + " : " + title )
    val lists = doc.select("#post_list .postids")
    val puts = new Array[Put](lists.size())
    for(list <- elements2List(lists)){
      val arr = new Array[(String, String, String)](10)
      temp = list.select("a.needonline").text().trim
      arr(0) = if(temp != null && temp.length > 0)  ("comments", "username", temp) else null
      temp = list.select(".user_atten li:eq(0)").text().trim
      arr(1) = if(temp != null && temp.length > 0) ("comments", "fans", NumExtractUtil.getNumArray(temp)(0)) else null
      temp = list.select(".user_atten li:eq(1)").text().trim
      arr(2) = if(temp != null && temp.length > 0) ("comments", "jinghua", NumExtractUtil.getNumArray(temp)(0)) else null
      temp = list.select(".user_atten li:eq(2)").text().trim
      arr(3) = if(temp != null && temp.length > 0) ("comments", "tiezi", NumExtractUtil.getNumArray(temp)(0)) else null
      temp = list.select(".post_time").text().trim
      arr(4) = if(temp != null && temp.length > 0) ("comments", "post-time",TimeUtil.getPostTime(temp) ) else null
      temp = list.select(".normal_msg table tbody tr:eq(0) td:eq(0) .replyBody span.cite font[color=#05A]").text().trim
      if(temp!=null && temp.length > 0){
        arr(7) = if(temp != null && temp.length > 0) ("comments", "replywho", temp.split(" ")(0)) else null
//        temp = list.select(".normal_msg td:eq(0) .replyBody span.cite font[color=#05A] a:eq(1)").text().trim
        arr(8) = if(temp != null && temp.length > 0) ("comments", "floor2", FloorUtil.getFloorNumber(temp.split(" ")(1))) else null
      }else{
        temp = list.select(".post_msg").text().trim
        arr(5) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
      }

      temp = list.select(".post_main .post_edition").text().trim
      arr(9) = if(temp != null && temp.length > 0) ("comments", "clientside", temp.substring(2)) else null

      temp = list.select(".post_floor em, .post_floor").text().trim
      arr(6) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null  //从 1 开始
//      ("comments", "", list.select("").text())
//      ("comments", "", list.select("").text())

      val time = arr(4)._3
      val key = "pcauto" + " " * 2 + "|" + luntan.substring(0, luntan.length - 2) + "|" + time + "|" + url + "|" + arr(6)._3
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
