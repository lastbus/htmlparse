package com.shgc.htmlparse.parse

import java.text.SimpleDateFormat

import com.shgc.htmlparse.util.{NumExtractUtil, FloorUtil, TimeUtil, Selector}
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
    var temp: String = null

    try {
      val luntan = doc.select("body #wp #pt div.z a:eq(8)").text().trim
      val problem = doc.select("body #wp #pt div.z a:last-child").text().trim
      val clickAndView = doc.select("#postlist table:first-child td:eq(0) span.xi1").text().trim

      val lists = doc.select("#postlist > div[id^=post_]")
      val putsArray = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](15)
        temp = list.select("table tbody tr:eq(0) td:eq(0) div.pi div.authi a.xw1").text().trim
        arr(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) table th:eq(0) a").text().trim

        arr(1) = if(temp != null && temp.length > 0) ("comments", "publish", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) table th:eq(1) a").text().trim
        arr(2) = if(temp != null && temp.length > 0) ("comments", "friends", temp) else null

//        temp = list.select("table tbody tr:eq(0) td:eq(0) table td a").text().trim  重复，去掉
//        arr(3) = if(temp != null && temp.length > 0) ("comments", "jifen", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) > div > p em a").text().trim
        arr(4) = if(temp != null && temp.length > 0) ("comments", "level", temp) else null

        temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(1)").text().trim
        arr(5) = if(temp != null && temp.length > 0) ("comments", "virtualmoeny", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(3)").text().trim
        arr(6) = if(temp != null && temp.length > 0) ("comments", "ji-fen", temp) else null
        temp = list.select("table tbody tr:eq(0) td:eq(0) > dl > dd:eq(5)").text().trim
        arr(7) = if(temp != null && temp.length > 0) ("comments", "lastlogin", TimeUtil.getBitAutoTime(temp)) else null

        temp = list.select("table tbody tr:eq(0) td:eq(1) div.pti div.authi em").text().trim
        arr(8) = if(temp != null && temp.length > 0) ("comments", "posttime", TimeUtil.getPostTime(temp)) else null
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

//        val carType = luntan
//        val time = arr(8)._3
        val key = "tencent" + " " + "|" + luntan + "#" * (8 - luntan.length) + "|" + arr(8)._3 +
          "|" + url + "|" + arr(9)._3
        val put = new Put(Bytes.toBytes(key))
        if (problem != null && problem.length > 0)
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("problem"), Bytes.toBytes(problem))
        if (clickAndView != null && clickAndView.length > 0) {
          val clickView = NumExtractUtil.getNumArray(clickAndView)
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


}
