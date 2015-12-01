package com.shgc.htmlparseTest.parse

import java.text.SimpleDateFormat

import com.shgc.htmlparse.util.{FloorUtil, TimeUtil}
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
    val url = "http://saa.auto.sohu.com/benben/thread-200621441159828-1.shtml"
    val html = Jsoup.connect(url).get().html()
    val doc = Jsoup.parse(html)
    var temp: String = null

    val luntan = doc.select("body .wapper980 .conmain .con-head h1 a").text()
    val problem = doc.select("body .wapper980 .conmain .con-head h1").text().trim.split(" ")
    val clickAndView = doc.select("body .wapper980 .conmain .con-head span.con-head-info").text()
    println("luntan: " + luntan.substring(0, luntan.length - 3))
    println("problem: " + problem(problem.size - 1))
    println("reply: " + clickAndView.split("/")(0).substring(1) + " view: " + clickAndView.split("/")(1).substring(1))

    val lists = doc.select("body div.wapper980 div.conmain div.con-wrap[id^=floor-]")
    val putsArray = new Array[Put](lists.size())
    println(s"there are ${lists.size()}   floors")
    var i = 0
    for(list <- elements2List(lists)){
      val arr = new Array[(String, String, String)](15)
      temp = list.select("div.con-side a.user-nickname").text().trim
      arr(0) = if(temp != null && temp.length >0) ("comments", "username", temp) else null
      temp = list.select("div.con-side p:contains(来自)").text().trim
      arr(1) = if(temp != null && temp.length >0) ("comments", "area", temp.substring(temp.indexOf("：") + 1)) else null
      temp = list.select("div.con-side p:contains(等级)").text().trim
      arr(2) = if(temp != null && temp.length >0) ("comments", "level",  temp.substring(temp.indexOf("：") + 1)) else null
      temp = list.select("div.con-side p:contains(注册)").text().trim
      arr(3) = if(temp != null && temp.length >0) ("comments", "register-time", TimeUtil.getBitAutoTime(temp)) else null
      temp = list.select("div.con-side p:contains(爱车)").text().trim
      arr(4) = if(temp != null && temp.length >0) ("comments", "ai-che", temp) else null
      temp = list.select("div.con-main-wapper span.floor-time").text().trim
      arr(5) = if(temp != null && temp.length >0) ("comments", "time", TimeUtil.getPostTime(temp)) else null
      temp = if(i ==0) "楼主" else list.select("div.con-main-wapper span.floor").text().trim
      arr(6) = if(temp != null && temp.length >0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else  null

      //1正常发言  2 回复上面楼层
      temp = list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]").text().trim
      if (temp.length < 1) {
        temp = list.select("div.con-main-wapper div.con-main div.main-bd").text().trim
        arr(7) = if(temp != null && temp.length >0) ("comments", "comment", temp) else null
      } else {
        arr(8) = ("comments", "replay-who", temp.split(" ")(0))
        val temp2 = list.select("div.con-main-wapper div.con-main div.main-bd").text().trim
        println(temp2.substring(temp.length))
        arr(9) = ("comments", "comment", temp2.substring(temp.length))
      }

      val carType = luntan.substring(0, luntan.length - 3)
      val time = arr(5)._3

      val key = "sohu" + " " * 4 +  "|" + carType + "#" * (8 - carType.length) + "|" + time +
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

  /*

  问题记录
  1 topic 把论坛名字也加上了；已解决， 原因：SoHuParse里没有按照这里的写
  2 只有 view 字段，没有reply字段(已解决) 原因：if 后面两条语句忘了加在{} 中。


   */

}
