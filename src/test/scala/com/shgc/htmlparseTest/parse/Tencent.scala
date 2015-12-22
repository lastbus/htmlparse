package com.shgc.htmlparseTest.parse

import java.text.SimpleDateFormat

import com.shgc.htmlparse.util.{NumExtractUtil, FloorUtil, TimeUtil}
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
class Tencent {
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

  @Test
  def run = {
    val url = "http://club.auto.qq.com/t-832008-1.htm"
    val html = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0")
                .get().html()
//    println(html)
    val doc = Jsoup.parse(html)

    var temp: String = null

    val luntan = doc.select("body #wp #pt div.z a:eq(8)").text()
    val vehicle = doc.select("body #wp #pt div.z a:eq(6)").text()
    val problem = doc.select("body #wp #pt div.z a:last-child").text()
    val clickAndView = doc.select("#postlist table:first-child td:eq(0) span.xi1").text()
    println("vehicle" + vehicle)
    println("luntan: " + luntan)
    println("problem: " + problem)
    println("replay: " + NumExtractUtil.getNumArray(clickAndView)(1) + " view: " +
      NumExtractUtil.getNumArray(clickAndView)(0))

    val lists = doc.select("#postlist > div[id^=post_]")
    val putsArray = new Array[Put](lists.size())
    println(s"there are ${lists.size()}   floors")
    var i = 0
    for(list <- elements2List(lists)){
      val arr = new Array[(String, String, String)](15)
      temp = list.select("table tbody tr:eq(0) td:eq(0) div.pi div.authi a.xw1").text().trim
      arr(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(0) table th:eq(0) a").text().trim

      arr(1) = if(temp != null && temp.length > 0) ("comments", "tie-zi-publish", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(0) table th:eq(1) a").text().trim
      arr(2) = if(temp != null && temp.length > 0) ("comments", "friends", temp) else null

      temp = list.select("table tbody tr:eq(0) td:eq(0) table td a").text().trim
      arr(3) = if(temp != null && temp.length > 0) ("comments", "ji-fen", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(0) > div > p em a").text().trim
      arr(4) = if(temp != null && temp.length > 0) ("comments", "level", temp) else null

      temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(1)").text().trim
      arr(5) = if(temp != null && temp.length > 0) ("comments", "jin-qian", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(3)").text().trim
      arr(6) = if(temp != null && temp.length > 0) ("comments", "ji-fen", temp) else null
      temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(5)").text().trim
      arr(7) = if(temp != null && temp.length > 0) ("comments", "last-login", TimeUtil.getBitAutoTime(temp)) else null

      temp = list.select("table tbody tr:eq(0) td:eq(1) div.pti div.authi em").text().trim
      arr(8) = if(temp != null && temp.length > 0) ("comments", "time", TimeUtil.getPostTime(temp)) else null
      temp = list.select("table tbody tr:eq(0) td:eq(1) div.pi > strong em").text().trim
      arr(9) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp, 1)) else null
      temp = list.select("table tbody tr:eq(0) td:eq(1) div.pct table").text().trim
      arr(10) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null


      //1正常发言  2 回复上面楼层
//      if(list.select("") == null){
//        arr(7) = ("comments", "comment", list.select("").text())
//      }else {
//        arr(8) = ("comments", "replay-who", list.select("").text())
//        val temp = list.select("").text()
//        arr(9) = ("comments", "comment", temp)
//      }

      val carType = luntan
      println(arr(8))
      val time = arr(8)._3

      val key = "tencent" + " " +  "|" + carType + "#" * (8 - carType.length) + "|" + time +
        "|" + url  +"|" + arr(9)._3
      println(key)
      val put = new Put(Bytes.toBytes(key))
      for(a <-arr if a != null ){
        put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        println(a)
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

  def getTime(timeString: String): String ={
    try{
      sdf2.format(sdf.parse(timeString))
    }catch {
      case _ : Exception  => {
        sdf2.format(sdf.parse(timeString.substring(3)))
      }
    }
  }



}
