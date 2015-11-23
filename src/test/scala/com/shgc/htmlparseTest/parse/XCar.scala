package com.shgc.htmlparseTest.parse

import org.apache.hadoop.hbase.client.Put
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
    val url = "http://www.xcar.com.cn/bbs/viewthread.php?tid=22304995"
    val html = Jsoup.connect(url).get().html()
    val doc = Jsoup.parse(html)

    val luntan = doc.select("#_img div.F_box_2 table h1 a").text()
    val problem = doc.select("#_img div.F_box_2 table h1").text().split("<")(0)
    val viewAndReplay = doc.select("#showPic span").text()

    val lists = doc.select("#delpost .F_box_2")
    val putsArray = new Array[Put](lists.size())
    for(list <- elements2List(lists)){

      val arr = new Array[(String, String, String)](15)
      arr(0) = ("comments", "username", list.select("td:eq(0) > a[onclick]").text())
      arr(1) = ("comments", "level", list.select("td:eq(0) p[data]").text())
      arr(2) = ("comments", "all", list.select("td:eq(0) div.smalltxt p:last-child").text())
      arr(3) = ("comments", "android", list.select("td:eq(1) table tbody tr:eq(0) td .right a").text())
      arr(4) = ("comments", "time", list.select("td:eq(1) table tbody tr:eq(0) td div[style]").text())
      arr(5) = ("comments", "floor", list.select("td:eq(1) table tbody tr:eq(1) td span.t_title1").text())
      //正常回复
      arr(6) = ("comments", "comment", list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1").text())

      //有引用别人回复
      arr(7) = ("comments", "huifuwho", list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1 div.msgbody div.msgheader a").text())
      arr(8) = ("comments", "", list.select("td:eq(1) table tbody tr:eq(1) td div.t_msgfont1").after("div.msgbody").text())
//      arr(9) = ("comments"

      for(a <- arr if a != null){
        println(a)
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
