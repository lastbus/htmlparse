package com.shgc.htmlparse.parse

import java.text.SimpleDateFormat

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/11/25.
 */
class QQParse extends Parser{

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

  override def run(content: Content, selector: Selector): Array[Put] = {

    val url = content.getUrl
    val html = new String(content.getContent, "utf-8")
    val doc = Jsoup.parse(html)

    try {
      val luntan = doc.select("body #wp #pt div.z a:eq(8)").text()
      val problem = doc.select("body #wp #pt div.z a:last-child").text()
      val clickAndView = doc.select("#postlist table:first-child td:eq(0) span.xi1").text()
      println("replay: " + clickAndView.split(" ")(1) + " view: " + clickAndView.split(" ")(0))

      val lists = doc.select("#postlist > div[id^=post_]")
      val putsArray = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](15)
        arr(0) = ("comments", "username", list.select("table tbody tr:eq(0) td:eq(0) div.pi div.authi a.xw1").text())
        arr(1) = ("comments", "zhu-ti", list.select("table tbody tr:eq(0) td:eq(0) table th:eq(0) a").text())
        arr(2) = ("comments", "friends", list.select("table tbody tr:eq(0) td:eq(0) table th:eq(1) a").text())

        arr(3) = ("comments", "ji-fen", list.select("table tbody tr:eq(0) td:eq(0) table td a").text())
        arr(4) = ("comments", "level", list.select("table tbody tr:eq(0) td:eq(0) > div > p em a").text())

        arr(5) = ("comments", "jin-qian", list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(1)").text())
        arr(6) = ("comments", "ji-fen", list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(3)").text())
        arr(7) = ("comments", "last-login", list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(5)").text())

        arr(8) = ("comments", "time", list.select("table tbody tr:eq(0) td:eq(1) div.pti div.authi em").text())
        arr(9) = ("comments", "floor", list.select("table tbody tr:eq(0) td:eq(1) div.pi > strong em").text())
        arr(10) = ("comments", "comment", list.select("table tbody tr:eq(0) td:eq(1) div.pct table").text())


        //1正常发言  2 回复上面楼层
        //      if(list.select("") == null){
        //        arr(7) = ("comments", "comment", list.select("").text())
        //      }else {
        //        arr(8) = ("comments", "replay-who", list.select("").text())
        //        val temp = list.select("").text()
        //        arr(9) = ("comments", "comment", temp)
        //      }

        val carType = luntan
        val time = getTime(arr(8)._3)
        val key = "qq" + " " * 6 + "|" + carType + "空" * (8 - carType.length) + "|" + time +
          "|" + url + "|" + arr(9)._3
        val put = new Put(Bytes.toBytes(key))
        if (problem != null && problem.length > 0)
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("problem"), Bytes.toBytes(problem))
        if (clickAndView != null && clickAndView.length > 0) {
          val clickView = clickAndView.split(" ")
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(clickView(1).substring(1)))
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("view"), Bytes.toBytes(clickView(0).substring(1)))
        }
        for (a <- arr if a != null) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
    }catch {
      case _ : Exception => return null
    }
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
