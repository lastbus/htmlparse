package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.{FloorUtil, TimeUtil, NumExtractUtil, Selector}
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


  override def run(content: Content, selector: Selector): Array[Put] = {
//    try {
      val html = new String(content.getContent, "utf-8")
      val url = content.getUrl
      val doc = Jsoup.parse(html)
      var temp: String = null
      temp = doc.select("#TitleForumLink").text().trim
      val luntan = if(temp != null && temp.length > 0) temp else null
      val topic = doc.select("#TitleTopicSt").text().trim
      val clickAndReply = doc.select("[class=title_box] span").text().trim

      val list = doc.select(".postcontbox .postcont_list")
      val putsArray = new Array[Put](list.size)
      var i = 0
      for (t <- elements2List(list) if t.select("[span:contains(已禁用)]") != null) {

        val contArray = new Array[(String, String, String)](10)
        temp = t.select("[class=user_name]").text().trim
        contArray(0) = if(temp != null && temp.length > 0) ("comments", "username", temp) else null
        temp = t.select("li:contains(等)").text().trim
        contArray(1) = if(temp != null && temp.contains("：")) ("comments", "level", temp.substring(temp.indexOf("：") + 1).trim) else null
        temp = t.select("li:contains(帖)").text().trim
        if(temp != null && temp.contains("精华")){
          val result = NumExtractUtil.getNumArray(temp)
          if(result.size == 2){
            contArray(2) =  ("comments", "publish", result(0))
            contArray(3) = ("comments", "jinghua", result(1))
          }
        }
        temp = t.select("li:contains(地)").text().trim
        contArray(9) = if(temp != null && temp.contains("：")) ("comments", "area",temp.substring(temp.indexOf("：") + 1).trim) else null
        temp = t.select("li:contains(车)").text().trim
        contArray(4) = if(temp != null && temp.contains("：")) ("comments", "car", temp.substring(temp.indexOf("：") + 1).trim) else null
        temp = t.select("li:contains(册)").text().trim
        contArray(5) = if(temp != null && temp.length > 0) ("comments", "registertime", TimeUtil.getBitAutoTime(temp)) else null
        temp = t.select("span[role=postTime]").text().trim
        contArray(6) = if(temp != null && temp.length > 0) ("comments", "posttime", TimeUtil.getPostTime(temp)) else null
        temp = t.select("div[class=post_width]").text().trim
        contArray(7) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
        temp = t.select("div[class=floor_box]").text().trim
        contArray(8) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else null

        val carType = luntan.substring(0, luntan.length - 2)
        val time = contArray(6)._3
        val key = "bitauto" + " " + "|" + carType + "#" * (8-carType.length) +
          "|" + time + "|" + url + "|" + contArray(8)._3

        val put = new Put(Bytes.toBytes(key))
        if(clickAndReply != null) {
          val t = NumExtractUtil.getNumArray(clickAndReply)
          put.addColumn(Bytes.toBytes("comment"), Bytes.toBytes("reply"), Bytes.toBytes(t(0)))
          put.addColumn(Bytes.toBytes("comment"), Bytes.toBytes("view"), Bytes.toBytes(t(1)))
        }
        put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(topic))

        for (arr <- contArray if arr != null) {
          put.addColumn(Bytes.toBytes(arr._1), Bytes.toBytes(arr._2), Bytes.toBytes(arr._3))
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
//    }catch {
//      case _ :Exception => return null
//    }
  }


}
