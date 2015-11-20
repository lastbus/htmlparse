package com.shgc.htmlparse.parse

import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/20.
 */
class AutoHomeParser extends Parser{

  override def run(content: Content, selector: Selector): Array[Put] = {
    val html = new String(content.getContent, selector.encoding)
    val url = content.getUrl
    val doc = Jsoup.parse(html)
    val list = doc.select(selector.body)
    val arrayBuffer = new ArrayBuffer[Put]
    for(element <- elements2List(list)){
      val put = new Put(Bytes.toBytes(url))
      for(sel <- selector.select){
        put.addColumn(Bytes.toBytes(sel._2),
          Bytes.toBytes(sel._3),
          Bytes.toBytes(element.select(sel._1).text()))
      }
      for(strategy <- selector.strategySelector){
//        val p =
      }
      arrayBuffer += put
    }
    arrayBuffer.toArray

    null
  }

//  def getColums(selector: Array[(String, String, String)], put: Put): Unit ={
//    for(sel <- selector){
//      put.addColumn(Bytes.toBytes(sel._2),
//        Bytes.toBytes(sel._3),
//        Bytes.toBytes(element.select(sel._1).text()))
//    }
//  }



  def getCarType(text: String): String ={
    val pattern = Pattern.compile("论坛")
    val matcher = pattern.matcher(text)
    if(matcher.find()) text.slice(0, matcher.start()) else text
  }
}
