package com.shgc.htmlparseTest.parse

import java.text.SimpleDateFormat

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.Test

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/25.
 */
@Test
class SoHu {
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

  @Test
  def run = {
    val url = "http://saa.auto.sohu.com/benben/thread-292971439450333-1.shtml"
    val html = Jsoup.connect(url).get().html()
    val doc = Jsoup.parse(html)

    val luntan = doc.select("body .wapper980 .conmain .con-head h1 a").text()
    val problem = doc.select("body .wapper980 .conmain .con-head h1").text()
    val clickAndView = doc.select("body .wapper980 .conmain .con-head span.con-head-info").text()
    println("luntan: " + luntan.substring(0, luntan.length - 3))
    println("problem: " + problem.split(">")(1))
    println("replay: " + clickAndView.split("/")(0).substring(1) + " view: " + clickAndView.split("/")(1).substring(1))

    val lists = doc.select("body div.wapper980 div.conmain div.con-wrap[id^=floor-]")
    val putsArray = new Array[Put](lists.size())
    println(s"there are ${lists.size()}   floors")
    var i = 0
    for(list <- elements2List(lists)){
      val arr = new Array[(String, String, String)](15)
      arr(0) = ("comments", "username", list.select("div.con-side a.user-nickname").text())
      arr(1) = ("comments", "area", list.select("div.con-side p:contains(来自)").text())
      arr(2) = ("comments", "level", list.select("div.con-side p:contains(等级)").text())
      arr(3) = ("comments", "register-time", list.select("div.con-side p:contains(注册)").text())
      arr(4) = ("comments", "ai-che", list.select("div.con-side p:contains(爱车)").text())
      if(list.select("div.con-side p:contains(爱车)").size() == 0 ){
        println("ai-che == null")
      }
      arr(5) = ("comments", "time", list.select("div.con-main-wapper span.floor-time").text())
      arr(6) = ("comments", "floor", if(i == 0) "楼主" else list.select("div.con-main-wapper span.floor").text())
      //1正常发言  2 回复上面楼层
      if(list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]") == null){
        arr(7) = ("comments", "comment", list.select("div.con-main-wapper div.con-main div.main-bd").text())
      }else {
        arr(8) = ("comments", "replay-who", list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]").text())
        val temp = list.select("div.con-main-wapper div.con-main div.main-bd").text()
        arr(9) = ("comments", "comment", temp.substring(temp.indexOf(arr(8)._3) + arr(8)._3.length))
      }

      val carType = luntan.substring(0, luntan.length - 3)
      val time = getTime(arr(5)._3)

      val key = "sohu" + " " * 4 +  "|" + carType + "空" * (8 - carType.length) + "|" + time +
                "|" + url  +"|" + arr(6)._3
      println(key)
      val put = new Put(Bytes.toBytes(key))
      for(a <-arr if a != null && a._3.length > 0){
        put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        println(a)
      }
      putsArray(i) = put
      i += 1
    }
//    putsArray
  }

  implicit def elements2List(elements: Elements): Array[Element] ={
    val arr = new ArrayBuffer[Element]
    val e = elements.listIterator()
    while(e.hasNext)
      arr += e.next()
    arr.toArray
  }

  def getTime(timeString: String): String ={
    sdf2.format(sdf.parse(timeString))
  }

}
