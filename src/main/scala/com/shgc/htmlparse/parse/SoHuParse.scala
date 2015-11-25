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
class SoHuParse extends Parser{

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")


  override def run(content: Content, selector: Selector): Array[Put] = {
    val url = content.getUrl
    val html = new String(content.getContent, "gbk")
    val doc = Jsoup.parse(html)

    try {
      val luntan = doc.select("body .wapper980 .conmain .con-head h1 a").text()
      val problem = doc.select("body .wapper980 .conmain .con-head h1").text()
      val clickAndView = doc.select("body .wapper980 .conmain .con-head span.con-head-info").text()

      val lists = doc.select("body div.wapper980 div.conmain div.con-wrap[id^=floor-]")
      val putsArray = new Array[Put](lists.size())
      var i = 0
      for (list <- elements2List(lists)) {
        val arr = new Array[(String, String, String)](10)
        arr(0) = ("comments", "username", list.select("div.con-side a.user-nickname").text())
        arr(1) = ("comments", "area", list.select("div.con-side p:contains(来自)").text())
        arr(2) = ("comments", "level", list.select("div.con-side p:contains(等级)").text())
        arr(3) = ("comments", "register-time", list.select("div.con-side p:contains(注册)").text())
        arr(4) = ("comments", "ai-che", list.select("div.con-side p:contains(爱车)").text())
        arr(5) = ("comments", "time", list.select("div.con-main-wapper span.floor-time").text())
        arr(6) = ("comments", "floor", if (i == 0) "楼主" else list.select("div.con-main-wapper span.floor").text())
        //1正常发言  2 回复上面楼层
        if (list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]") == null) {
          arr(7) = ("comments", "comment", list.select("div.con-main-wapper div.con-main div.main-bd").text())
        } else {
          arr(8) = ("comments", "replay-who", list.select("div.con-main-wapper div.con-main div.main-bd div[flag=true]").text())
          val temp = list.select("div.con-main-wapper div.con-main div.main-bd").text()
          arr(9) = ("comments", "comment", temp.substring(temp.indexOf(arr(8)._3) + arr(8)._3.length))
        }

        val carType = luntan.substring(0, luntan.length - 3)
        val time = getTime(arr(5)._3)

        val key = "sohu" + " " * 4 + "|" + carType + "空" * (8 - carType.length) + "|" + time +
          "|" + url + "|" + arr(6)._3
        val put = new Put(Bytes.toBytes(key))
        if (problem != null && problem.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("problem"), Bytes.toBytes(problem))
        if (clickAndView != null && clickAndView.length > 0) {
          val clickView = clickAndView.split("/")
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(clickView(0).substring(1)))
          put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("view"), Bytes.toBytes(clickView(1).substring(1)))
        }
        for (a <- arr if a != null && a._3.length > 0) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
        }
        putsArray(i) = put
        i += 1
      }
      return putsArray
    }catch {
      case _ : Exception => return  null
    }
  }

  def getTime(timeString: String): String ={
    sdf2.format(sdf.parse(timeString))
  }

}
