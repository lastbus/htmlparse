package com.shgc.htmlparseTest.jsoupTest

import java.io.{InputStreamReader, BufferedReader}
import java.net.URL
import java.util.regex.Pattern

import com.shgc.htmlparse.parse.Qy58Parser
import com.shgc.htmlparse.util.ParseConfiguration
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer


/**
 * Created by Administrator on 2015/11/20.
 */
@Test
class JsoupTest {



//  @Test
  def main2: Unit ={
    val url = "http://club.autohome.com.cn/bbs/thread-c-3217-46839120-1.html"
    //    require(ParseConfiguration.readConf())
    //    val selectorMap = ParseConfiguration.urlMap.toMap
    //    val selector = selectorMap.filter(_._1.matcher(url).matches()).head._2
    val html = Jsoup.connect(url).get().toString
    val doc = Jsoup.parse(html)
    val bbsName = doc.select("#a_bbsname")
    val matcher = Pattern.compile("论坛").matcher(bbsName.text())
    println(bbsName.text())
    if(matcher.find()){
      println(bbsName.text().slice(0,matcher.start()))
    }

    val click = doc.select("#x-views")
    println(click.text())
    val replys = doc.select("#x-replys")
    println(replys.text())
    val problem = doc.select("#consnav span").last()
    println(problem.text())
    println("\n\n\n")

    val body = doc.select("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F]")
    for(b <- elements2List(body)){
      println(b.select("[class=txtcenter fw]").text())//name
      println(b.select(".lv-txt").text()) //level

      println(b.select("li:contains(精华)").text()) //精华
      println(b.select("li:contains(帖子)").text()) //帖子
      println(b.select("li:contains(注册)").text()) //register time
      println(b.select("li:contains(来自)").text()) //area
      println(b.select("li:contains(所属)").text()) //
      println(b.select("li:contains(关注)").text().trim) //
      println(b.select("li:contains(爱车)").text().trim) //
      println(b.select("span[xname=date]").text().trim) // 发表时间
      println(b.select("a[class=rightbutlz fr], div[class=fr]").text())
      println(b.select("div[class=plr26 rtopconnext] span:contains(来自) a").text) //手机客户端

      //设计评论部分
      print(b.select(".w740 .relyhfcon p a:contains(楼)").text())
      println(b.select(".w740 .rrlycontxt").text())
      println(b.select(".w740 .yy_reply_cont").text())

      println(b.select(".w740").text())
//      println(b.select(""))

      println("\n")


    }

















    //    val content = new Content()
    //    assertEquals("gb2312", selector.encoding)
    //    content.setContent(html.getBytes(selector.encoding))
    //    val parser = new Qy58Parser
    //    val puts = parser.parse((url,content), selectorMap)



    //    for(put <- puts){
    //      val p = put.getRow
    //      println(p)
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("username")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("floor")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("time")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("registerTime")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("car")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("area")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("comment")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("")))
    //      println(put.get(Bytes.toBytes("comments"), Bytes.toBytes("")))

    //    }





  }

  implicit def elements2List(elements: Elements): Array[Element] ={
    val arr = new ArrayBuffer[Element]
    val e = elements.listIterator()
    while(e.hasNext)
      arr += e.next()
    arr.toArray
  }



}


object JsoupTest{

  def getHtml(u: String): String ={
    val url = new URL(u)
    val in = url.openConnection()
    val inputStream = in.getInputStream
    val bufferReader = new BufferedReader(new InputStreamReader(inputStream))
    val sb = new StringBuilder
    var temp: String = null
    do {
      temp = bufferReader.readLine()
      sb.append(temp)
    }while(temp != null)

    sb.toString()
  }



}