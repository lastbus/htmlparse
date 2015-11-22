package com.shgc.htmlparse.parse

import java.net.URL
import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/20.
 */
class AutoHomeParser extends Parser{

  override def run(content: Content, selector: Selector): Array[Put] = {
    val html = new String(content.getContent, selector.encoding)
    val url = content.getUrl
    val host = new URL(url).getHost
    val putsArrayBuffer = new ArrayBuffer[Put]
    val keys = selector.keys
    val doc = Jsoup.parse(html)
    val separator = selector.separator

    val list = doc.select(selector.body)
    for(element <- elements2List(list)){
      //将取出的值放入一个临时的 ArrayBuffer 中
      val values = new ArrayBuffer[(String, String, String)]

      for((select, columnFamily, column) <- selector.select){
        values += ((columnFamily, column, element.select(select).text()))
      }

      //一种情况是回复别人，另一种是发言
      if(element.select(selector.strategySelector(0)(0)._1).text().length > 0) {
        for (sel <- selector.strategySelector(0)) {
          values += ((sel._2, sel._3, element.select(sel._1).text()))
        }
      }else{
        for(sel <- selector.strategySelector(1)){
          values += ((sel._2, sel._3, element.select(sel._1).text()))
        }
      }

      val keyBuffer = new StringBuilder(host + separator)
      //生成主键
      for(key <- keys){
        for(col <- values if (col._2.equalsIgnoreCase(key))){
          keyBuffer.append(col + separator)
        }
      }
      //make the Put
      val put = new Put(Bytes.toBytes(keyBuffer.toString()))
      for((columnFamily, column, value) <- values){
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value))
      }
      putsArrayBuffer += put
    }
    putsArrayBuffer.toArray

    null
  }




  def getCarType(text: String): String ={
    val pattern = Pattern.compile("论坛")
    val matcher = pattern.matcher(text)
    if(matcher.find()) text.slice(0, matcher.start()) else text
  }
}
