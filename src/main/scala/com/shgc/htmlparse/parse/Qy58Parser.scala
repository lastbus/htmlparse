package com.shgc.htmlparse.parse

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.LogManager
import java.util.regex.Pattern
import org.apache.nutch.protocol.Content
import org.jsoup.Jsoup
import scala.collection.mutable.ArrayBuffer

/**
 * Created by make on 2015/11/18.
 */
class Qy58Parser extends Parser{
  @transient val LOG = LogManager.getLogger(this.getClass.getName)
  var urlMap: Map[Pattern, Selector] = null

  /**
   * 解析网页的业务逻辑部分
   * @param content
   * @param selector
   * @return
   */
  @Override
  def run(content: Content, selector: Selector): Array[Put] ={
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
      arrayBuffer += put
    }
    arrayBuffer.toArray
  }

}
