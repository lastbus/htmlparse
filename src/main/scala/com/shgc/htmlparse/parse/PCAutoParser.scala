package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/11/24.
 */
class PCAutoParser extends Parser {
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

  override def run(content: Content, selector: Selector): Array[Put] = {

    try {
      val html = new String(content.getContent, "gb2312")
      val url = content.getUrl
      val baseUrl = content.getBaseUrl
      val host = new URL(url).getHost
      val doc = Jsoup.parse(html)

      val luntan = doc.select("#content .com-crumb a").text().split(" ")(3)
      val view = doc.select("#views").text() // kan bu dao
      val replay = doc.select(".overView span").last().text()
      val title = doc.select(".post_right .post_r_tit .yh").text()


      val lists = doc.select("#post_list .postids")
      val puts = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
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
        if (title != null) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(title))
        if (view != null) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(view))
        if (replay != null) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(replay))

        for (c <- arr if c != null && c._3 != null) {
          put.addColumn(Bytes.toBytes(c._1), Bytes.toBytes(c._2), Bytes.toBytes(c._3))
        }
        puts(i) = put
        i += 1
      }

      return puts
    }catch {
      case _ :Exception => return null
    }
    }

}
