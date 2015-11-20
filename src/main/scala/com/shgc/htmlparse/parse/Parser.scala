package com.shgc.htmlparse.parse

import java.util.regex.Pattern

import com.shgc.htmlparse.util.Selector
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.io.Text
import org.apache.nutch.protocol.Content
import org.apache.spark.rdd.RDD
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.mutable.ArrayBuffer

/**
 * Created by make on 2015/11/18.
 */
trait Parser extends Serializable{

  def parse(rdd: RDD[(Text,Content)], urlSelectorMap: Map[Pattern, Selector]): RDD[(ImmutableBytesWritable ,Put)] ={
    rdd.map{case (k, v) => parse((k.toString,v), urlSelectorMap)}.filter(_!= null).flatMap(put => put).
      map(put => (new ImmutableBytesWritable(put.getRow), put))
  }

  def run(content: Content, selector: Selector): Array[Put]

  final def parse(html: (String, Content), urlSelectorMap: Map[Pattern, Selector]): Array[Put] = {
    if(urlSelectorMap == null) return null
    val selector = filter(html._1, urlSelectorMap)
    if(selector == null) null else run(html._2, selector)
  }

  /**
   * 根据 url 匹配解析器
   * @param url
   * @return
   */
  final def filter(url: String, urlSelectorMap: Map[Pattern, Selector]): Selector ={
    for((urlPattern, selector) <- urlSelectorMap){
      if(urlPattern.matcher(url).matches()) return selector
    }
    null
  }

  /**
   * 将 Jsoup 的 Elements 转换为scala Array
   * @param elements
   * @return
   */
  final def elements2List(elements: Elements): Array[Element] ={
    val arr = new ArrayBuffer[Element]
    val e = elements.listIterator()
    while(e.hasNext)
      arr += e.next()
    arr.toArray
  }

}
