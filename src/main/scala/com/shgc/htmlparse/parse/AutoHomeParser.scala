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
import org.jsoup.nodes.Element

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/20.
 */
class AutoHomeParser extends Parser{
  @transient val LOG = LogManager.getLogger(this.getClass.getName)
  var urlMap: Map[Pattern, Selector] = null


  override def run(content: Content, selector: Selector): Array[Put] = {
    val html = new String(content.getContent, selector.encoding)
    val url = content.getUrl
    val putsArrayBuffer = new ArrayBuffer[Put]
    val keys = selector.keys
    val doc = Jsoup.parse(html)
    val separator = selector.separator

    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")

    var floor: String = null
    val list = doc.select(selector.body)
    for(element <- elements2List(list)){
      //将取出的值放入一个临时的 ArrayBuffer 中
      val values = new ArrayBuffer[(String, String, String)]

      for((select, columnFamily, column) <- selector.select){
        values += ((columnFamily, column, element.select(select).text()))
        if(column.equalsIgnoreCase("FLOOR")) floor = formatFloor(values.last._3)
      }

      //一种情况是回复别人，另一种是发言
      if(element.select(selector.strategySelector(0)(0)._1).text() != null) {
        for (sel <- selector.strategySelector(0)) {
          values += ((sel._2, sel._3, element.select(sel._1).text()))
        }
      }else{
        for(sel <- selector.strategySelector(1)){
          values += ((sel._2, sel._3, element.select(sel._1).text()))
        }
      }
      val host = new URL(url).getHost
      val keyBuffer = new StringBuilder(host + separator)
      //生成主键
      for(key <- keys){
        for(col <- values if (col._2.equalsIgnoreCase(key))){
          if(key.equalsIgnoreCase("TIME")){
            keyBuffer.append(sdf2.format(sdf.parse(col._3)) + separator)
          } else if(key.equalsIgnoreCase("CARTYPE")){
            keyBuffer.append(col._3.substring(0, col._3.length - 2) + separator)
          }else keyBuffer.append(col._3 + separator)
        }
      }
      val k = keyBuffer.append(url).append(floor)
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

  def formatFloor(floor: String): String = {
    if(floor.equals("楼主")){
      "0"
    }else if(floor.equals("沙发")){
      "1"
    }else if(floor.equals("板凳")){
      "2"
    }else if(floor.equals("地板")){
      "3"
    }else floor.substring(0, floor.length - 2)
  }
}
