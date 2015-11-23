package com.shgc.htmlparse.parse

import java.net.URL
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.LogManager
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/23.
 */
class AutoHomeParser2 extends Parser {

  @transient val LOG = LogManager.getLogger(this.getClass.getName)
  var urlMap: Map[Pattern, Selector] = null
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val pattern = Pattern.compile("论坛")

  override def run(content: Content, selector: Selector): Array[Put] = {
    val html = new String(content.getContent, "gb2312")
    val url = content.getUrl
    val putsArrayBuffer = new ArrayBuffer[Put]
    val commentsArray = new Array[(String, String, String)](16)
    val doc = Jsoup.parse(html)


    val bbsName = doc.select("#a_bbsname")
    val matcher = pattern.matcher(bbsName.text())
    var bbsName2: String = null
    if (matcher.find()) {
      val s = bbsName.text().slice(0, matcher.start())
      bbsName2 = s + "空" * (5 - s.length)
    }

    val click = doc.select("#x-views").text() //
    val replays = doc.select("#x-replys").text() //
//    val problem = doc.select("#consnav span").last().text() //

//    val columnFamily = "comments"
    val body = doc.select("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F]")
    for (b <- elements2List(body)) {
      commentsArray(0) = ("columnFamily","username",b.select("[class=txtcenter fw]").text()) //name
      commentsArray(1) = ("columnFamily","level",b.select(".lv-txt").text()) //level

      commentsArray(2) = ("columnFamily","jinghua",b.select("li:contains(����)").text()) //
      commentsArray(3) = ("columnFamily","tiezi",b.select("li:contains(����)").text()) //
      commentsArray(4) = ("columnFamily","register-time",b.select("li:contains(ע��)").text()) //register time
      commentsArray(5) = ("columnFamily","come-from",b.select("li:contains(����)").text()) //area
      commentsArray(6) = ("columnFamily","belong",b.select("li:contains(����)").text()) //
      commentsArray(7) = ("columnFamily","guanzhu",b.select("li:contains(��ע)").text().trim) //
      commentsArray(8) = ("columnFamily","aiche",b.select("li:contains(����)").text().trim) //
      commentsArray(9) = ("columnFamily","time",b.select("span[xname=date]").text().trim) //
      commentsArray(10) = ("columnFamily","bbsName",b.select("a[class=rightbutlz fr], div[class=fr]").text())  //
      commentsArray(11) = ("columnFamily","kehuduan",b.select("div[class=plr26 rtopconnext] span:contains(����) a").text) //

      //
      commentsArray(12) = ("columnFamily","jilou",b.select(".w740 .relyhfcon p a:contains(¥)").text())
      if(commentsArray(12) == null){
        commentsArray(15) = ("columnFamily","",b.select(".w740").text())
      }else{
        commentsArray(13) = ("columnFamily","neirong",b.select(".w740 .rrlycontxt").text())
        commentsArray(14) = ("columnFamily","huifu",b.select(".w740 .yy_reply_cont").text())
      }

      val host = new URL(url).getHost
      val time = sdf2.format(sdf.parse(commentsArray(9)._3))
      val key = host + "|" + bbsName2 + "|" + time + "|" + url + "|" + commentsArray(10)._3

      val put = new Put(Bytes.toBytes(key))
      put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("click"), Bytes.toBytes(click))
      put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("replay"), Bytes.toBytes(replays))
//      put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("problem"), Bytes.toBytes(problem))

      for(arr <- commentsArray ){
        if(arr != null )put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes(arr._2), Bytes.toBytes(arr._3))
      }

      putsArrayBuffer += put
    }

    putsArrayBuffer.toArray
  }

}