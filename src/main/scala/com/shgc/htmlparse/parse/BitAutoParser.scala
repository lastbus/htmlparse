package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/24.
 */
class BitAutoParser extends Parser{

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val pattern = Pattern.compile("")

  override def run(content: Content, selector: Selector): Array[Put] = {
    try {
      val html = new String(content.getContent, "utf-8")
      val url = content.getUrl
      val baseUrl = content.getBaseUrl
      val host = new URL(url).getHost
      val doc = Jsoup.parse(html)
      val luntan = doc.select("#TitleForumLink").text()
      val topic = doc.select("#TitleTopicSt").text()

      val clickAndReplay = doc.select("[class=title_box] span").text().split("/")


      val list = doc.select(".postcontbox .postcont_list")
      val putsArray = new Array[Put](list.size)
      var i = 0
      for (t <- elements2List(list) if t.select("[span:contains(已禁用)]") != null) {

        val contArray = new Array[(String, String, String)](10)
        contArray(0) = ("comments", "username", t.select("[class=user_name]").text())
        contArray(1) = ("comments", "level", t.select("li:contains(等)").text())
        contArray(2) = ("comments", "tiezi", t.select("li:contains(帖)").text())
        contArray(3) = ("comments", "area", t.select("li:contains(地)").text())
        contArray(4) = ("comments", "car", t.select("li:contains(车)").text())
        contArray(5) = ("comments", "registerTime", t.select("li:contains(册)").text())
        contArray(6) = ("comments", "time", t.select("span[role=postTime]").text()) //
        contArray(7) = ("comments", "comment", t.select("div[class=post_width]").text()) //
        contArray(8) = ("comments", "floor", t.select("div[class=floor_box]").text()) //
        val carType = luntan.substring(0, luntan.length - 2)
        val time = getTime(contArray(6)._3)
        val key = host + " " * (20 - host.length) + "|" + carType + " " * (8 - carType.length) +
          "|" + time + "|" + url.substring(baseUrl.length) + "|" + contArray(8)
        val put = new Put(Bytes.toBytes(key))
        if (clickAndReplay.size > 1) {
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(clickAndReplay(1)))
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(clickAndReplay(0)))
        }
        put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(topic))
        for (arr <- contArray if arr != null) {
          put.addColumn(Bytes.toBytes(arr._1), Bytes.toBytes(arr._2), Bytes.toBytes(arr._3))
          println(arr._3)
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
    }catch {
      case _ :Exception => return null
    }
  }

  def getTime(timeString: String): String ={
    try{
      sdf2.format(sdf.parse(timeString))
    }catch {
      case _ : Exception  => sdf2.format(sdf.parse(timeString.substring(3)))
    }
  }

}
