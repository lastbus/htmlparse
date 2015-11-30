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
    val url = "http://club.autohome.com.cn/bbs/thread-c-3217-46839120-1.html"
    val html = Jsoup.connect(url).get().toString
    val doc = Jsoup.parse(html)

    var temp: String = null
    temp = doc.select("#a_bbsname").text().split(" ")(0)
    val carType = if(temp != null && temp.length > 2) temp.substring(0, temp.length - 2) else null
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
      temp = b.select("li:contains(关注)").text().trim
      arr(7) = if(temp != null && temp.length > 3) ("comments", "guanzhu", temp.substring(3)) else null
      temp = b.select("li:contains(爱车)").text().trim
      arr(8) = if(temp != null && temp.length > 3) ("comments", "aiche", temp.substring(3)) else null
      temp = b.select("span[xname=date]").text().trim
      arr(9) = if(temp != null && temp.length > 0) ("comments", "posttime", TimeUtil.getPostTime(temp)) else null
      temp = b.select("a[class=rightbutlz fr], div[class=fr]").text().trim
      arr(10) = if(temp != null && temp.length > 0) ("comments", "floor", FloorUtil.getFloorNumber(temp)) else null
      temp = b.select("div[class=plr26 rtopconnext] span:contains(来自) a").text.trim
      arr(11) = if(temp != null && temp.length > 0) ("comments", "clientside", temp) else null //手机客户端
      //设计评论部分
      if (b.select(".w740 .relyhfcon p a:contains(楼)") != null) {
        temp = b.select(".w740 .relyhfcon p a:contains(楼)").text().trim
        arr(12) = if(temp != null && temp.length > 0) ("comments", "floor2", temp.substring(0, temp.length - 1)) else null
        temp = b.select(".w740 .rrlycontxt").text().trim
        arr(13) = if(temp != null && temp.length > 0) ("comments", "comment2", temp) else null
        temp =  b.select(".w740 .yy_reply_cont").text().trim
        arr(14) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else null
      } else {
        temp = b.select(".w740").text().trim
        arr(15) = if(temp != null && temp.length > 0) ("comments", "comment", temp) else  null
      }
      if(arr(9) != null && arr(10) != null && arr(9)._3 != null && arr(10)._3 != null){
        val key = "autohome"  + "|" + carType + "#" * (8 - carType.length) + "|" +
          arr(9)._3 + "|" + url + "|" + arr(10)._3

        val put = new Put((Bytes.toBytes(key)))

        if (click != null && click.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(click))
        if (replay != null && replay.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(replay))
        if(topic != null  && topic.length > 0) put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("topic"), Bytes.toBytes(topic))

        for (a <- arr if a != null && a._3 != null) {
          put.addColumn(Bytes.toBytes(a._1), Bytes.toBytes(a._2), Bytes.toBytes(a._3))
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
