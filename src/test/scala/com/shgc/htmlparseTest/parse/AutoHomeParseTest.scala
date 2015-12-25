package com.shgc.htmlparseTest.parse

import com.shgc.htmlparse.parse.AutoHomeParser
import com.shgc.htmlparse.util.{FloorUtil, TimeUtil, NumExtractUtil}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/23.
 */
@Test
class AutoHomeParseTest {

  @Test
  def testRun = {
    val url = "http://club.autohome.com.cn/bbs/thread-c-2119-24634071-1.html"
    val html = Jsoup.connect(url).get().toString
    val doc = Jsoup.parse(html)

    var temp: String = null
    temp = doc.select("#a_bbsname").text().trim
    val carType = if(temp.contains("论坛")) temp.substring(0, temp.indexOf("论坛")).trim else null

    temp = doc.select("#x-views").text()
    val click = if(temp != null  && temp.length > 0) temp else null
    temp = doc.select("#x-replys").text()
    val replay = if(temp != null  && temp.length > 0) temp else null
    //帖名
    val problem = doc.select("#consnav span")
    val topic = if(problem != null && problem.size > 0 ) problem.last().text() else null

    val body = doc.select("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F]")
    val putsArray = new Array[Put](body.size)
    var i = 0
    for (b <- elements2List(body)) {
      val arr = new Array[(String, String, String)](17)

      temp = b.select("[class=txtcenter fw]").text().trim
      arr(0) = if (temp != null && temp.length > 0) ("comments", "username", temp) else null  //username
      temp = b.select(".lv-txt").text().trim
      arr(1) = if (temp != null && temp.length > 0) ("comments", "level",temp ) else null  //level
      //其他称号
      if(arr(1) == null){
        temp = b.select(".txtcenter:last-child").text().trim
        arr(1) = if (temp != null && temp.length > 0) ("comments", "level",temp ) else null
      }

      val jingHua = NumExtractUtil.getNumArray(b.select("li:contains(精华)").text())
      if (jingHua != null && jingHua.size == 0) arr(2) = ("comments", "jinghua", jingHua(0))  //精华
      val tieZi = NumExtractUtil.getNumArray(b.select("li:contains(帖子)").text())
      if(tieZi.size == 2 ) {
        arr(3) = ("comments", "publish", tieZi(0)) //发布帖子数
        arr(16) = ("comments", "response", tieZi(1)) //回复帖子数
      }

      temp = b.select("li:contains(注册)").text().trim
      arr(4) =  if(temp != null && temp.length > 0) ("comments", "registertime", TimeUtil.getAutoHomeRT(temp)) else null//register time
      temp = b.select("li:contains(来自)").text().trim
      arr(5) = if(temp != null && temp.length > 3) ("comments", "area", temp.substring(3)) else null
      temp = b.select("li:contains(所属)").text().trim
      arr(6) = if(temp != null && temp.length > 3) ("comments", "suoshu", temp.substring(3)) else null
      temp = b.select("li:matchesOwn(关注)").text().trim
      arr(7) = if(temp != null && temp.length > 3) ("comments", "guanzhu", temp.substring(3)) else null
      temp = b.select("li:contains(爱车)").text().trim
      arr(8) = if(temp != null && temp.length > 3) ("comments", "aiche", temp.substring(3).split(" ")(0)) else null
      temp = b.select("span[xname=date]").text().trim
      arr(9) = if(temp != null && temp.length > 0) ("comments", "posttime", TimeUtil.getPostTime(temp)) else null
      temp = b.select("a[class=rightbutlz fr], div[class=fr]").text().trim
      arr(10) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else null
      temp = b.select("div[class=plr26 rtopconnext] span:contains(来自) a").text.trim
      arr(11) = if(temp != null && temp.length > 0) ("comments", "clientside", temp) else null //手机客户端
      //设计评论部分
      temp = b.select(".w740 .relyhf, .w740 .quote").text().trim
//      println(temp)
      if ( temp != null && temp.length > 0) {
        //回复楼上
        arr(12) = ("comments", "floor2", FloorUtil.getFloorNumber(temp))
        temp = b.select(".w740 .relyhfcon p:eq(0) a:eq(0)").text().trim
        arr(13) = if(temp != null && temp.length > 0) ("comments", "replywho", temp) else null
        temp =  b.select(".w740 .yy_reply_cont").text().trim
//        temp = b.select(".w740").outerHtml()
        arr(14) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
      } else {
        println("no replys")
        temp = b.select(".w740").text().trim
        arr(15) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else  null
        if(arr(15) == null){
//          println(b.select(".rconten"))
          temp = b.select(".rconten").text().trim
          arr(15) = if(temp != null && temp.length > 0 && temp.indexOf("发表于") == -1) ("comments", "comment", temp) else  null
        }
      }
      if(arr(9) != null && arr(10) != null && arr(9)._3 != null && arr(10)._3 != null){
        val key = "autohome"  + "|" + carType + "#" * (8 - carType.length) + "|" +
          arr(9)._3 + "|" + url + "|" + arr(10)._3
        println(key)
        val put = new Put((Bytes.toBytes(key)))

        if (click != null && click.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(click))
        if (replay != null && replay.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(replay))
        if(topic != null  && topic.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(topic))

        for (a <- arr if a != null && a._3 != null) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
          println(a)
        }

        putsArray(i) = put
        i += 1
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
