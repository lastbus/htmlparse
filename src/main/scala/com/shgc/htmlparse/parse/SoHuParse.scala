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
class SoHuParse extends Parser{

  override def run(content: Content, selector: Selector): Array[Put] = {
    val url = content.getUrl
    val html = new String(content.getContent, "gbk")
    val doc = Jsoup.parse(html)
    var temp: String = null
//    try {
      val luntan = doc.select("body .wapper980 .conmain .con-head h1 a").text().trim
      val carType = luntan.substring(0, luntan.length - 3)
      val problem = doc.select("body .wapper980 .conmain .con-head h1").text().trim.split(" ")
      val topic = problem(problem.size - 1)

      val clickAndView = doc.select("body .wapper980 .conmain .con-head span.con-head-info").text().trim

      val lists = doc.select("body div.wapper980 div.conmain div.con-wrap[id^=floor-]")
      val putsArray = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](10)
        temp = list.select("div.con-side a.user-nickname").text().trim
        arr(0) = if(temp != null && temp.length >0) ("comments", "username", temp) else null
        temp = list.select("div.con-side p:contains(来自)").text().trim
        arr(1) = if(temp != null && temp.length >0) ("comments", "area", temp.substring(temp.indexOf("：") + 1)) else null
        temp = list.select("div.con-side p:contains(等级)").text().trim
        arr(2) = if(temp != null && temp.length >0) ("comments", "level",  temp.substring(temp.indexOf("：") + 1)) else null
        temp = list.select("div.con-side p:contains(注册)").text().trim
        arr(3) = if(temp != null && temp.length >0) ("comments", "registertime", TimeUtil.getBitAutoTime(temp)) else null
        temp = list.select("div.con-side p:contains(爱车)").text().trim
        arr(4) = if(temp != null && temp.length >0) ("comments", "aiche", temp) else null
        temp = list.select("div.con-main-wapper span.floor-time").text().trim
        arr(5) = if(temp != null && temp.length >0) ("comments", "posttime", TimeUtil.getPostTime(temp)) else null
        temp = if(i ==0) "楼主" else list.select("div.con-main-wapper span.floor").text().trim
        arr(6) = if(temp != null && temp.length >0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else  null

        //1正常发言  2 回复上面楼层
        temp = list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]").text().trim
        if (temp.length < 1) {
          temp = list.select("div.con-main-wapper div.con-main div.main-bd").text().trim
          arr(7) = if(temp != null && temp.length >0) ("comments", "comment", temp) else null
        } else {
          arr(8) = ("comments", "replywho", temp.split(" ")(0))
          val temp2 = list.select("div.con-main-wapper div.con-main div.main-bd").text().trim
          println(temp2.substring(temp.length))
          arr(9) = ("comments", "comment", temp2.substring(temp.length))
        }

        val time = arr(5)._3

        val key = "sohu" + " " * 4 + "|" + carType + "#" * (8 - carType.length) + "|" + time +
          "|" + url + "|" + arr(6)._3

        val put = new Put(Bytes.toBytes(key))
        if (topic != null && topic.length > 0) {
          put.addColumn(Bytes.toBytes("comments"),
            Bytes.toBytes("topic"), Bytes.toBytes(topic))
        }
        if (clickAndView != null && clickAndView.length > 0) {
          val clickView = NumExtractUtil.getNumArray(clickAndView)
          if(clickAndView != null && clickAndView.length == 2) {
            put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("reply"), Bytes.toBytes(clickView(0)))
            put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("view"), Bytes.toBytes(clickView(1)))
          }
        }
        for (a <- arr if a != null && a._3.length > 0) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
//    }catch {
//      case _ : Exception => return  null
//    }
  }


}
