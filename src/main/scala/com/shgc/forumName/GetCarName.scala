package com.shgc.forumName

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/12/15.
 */
trait GetCarName {

  def get(url: String): Array[(String, Array[String])]

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
