package com.shgc.htmlparseTest.util

import java.util.regex.Pattern

import com.shgc.htmlparse.util.{Selector, ParseConfiguration}
import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

/**
 * Created by Administrator on 2015/11/18.
 */
@Test
class ParseConfigurationTest {

  @Test
  def readConf() ={
    assert(ParseConfiguration.readConf())
    val urlMap = ParseConfiguration.urlMap
    val url1 = "http://qy.58.com/22bj/"
    val url2 = "http://club.autohome.com.cn/bbs/threadqa-c-3068-47198448-1.html"
    var selector: Selector = null

    //test http://qy.58.com/
    testUrl1(url1, urlMap)
    //test http://club.autohome.com.cn/
//    testAutoHome(url2, urlMap)

    val hdfs2HBaseMap = ParseConfiguration.hdfs2HBaseMap
    val path1 = "/user/nutch/qy58/crawl/segments/2*/content/part*/data"
//    val value = hdfs2HBaseMap(path1)
//    assertEquals("hh", value._1)
//    assertEquals("forumParser", value._2)


  }

  def testUrl1(url1: String, urlMap: mutable.HashMap[Pattern, Selector]): Unit = {
    var selector: Selector = null
    for ((url, select) <- urlMap) {
      if (url.matcher(url1).matches()) selector = select
    }

    assertEquals("utf-8", selector.encoding)
    assertEquals("body", selector.body)

    assertEquals("#crumbs a:eq(1)", selector.select(0)._1)
    assertEquals("comments", selector.select(0)._2)
    assertEquals("area", selector.select(0)._3)

    assertEquals("h1", selector.select(1)._1)
    assertEquals("comments", selector.select(1)._2)
    assertEquals("company", selector.select(1)._3)
  }

  def testAutoHome(url1: String, urlMap: mutable.HashMap[Pattern, Selector]): Unit = {
    var selector: Selector = null
    for ((url, select) <- urlMap) {
      if (url.matcher(url1).matches()) selector = select
    }

    assert(selector != null)
    assertEquals("gb2312", selector.encoding)
    assertEquals("body #topic_detail_main #content #cont_main div[id^=maxwrap] div[id^=F", selector.body)

    assertEquals("[class=txtcenter fw]", selector.select(0)._1)
    assertEquals("comments", selector.select(0)._2)
    assertEquals("username", selector.select(0)._3)

    assertEquals("a[class=rightbutlz fr], div[class=fr]", selector.select(1)._1)
    assertEquals("comments", selector.select(1)._2)
    assertEquals("floor", selector.select(1)._3)


    assertEquals("|", selector.separator)
    assertEquals("car", selector.keys(0))
    assertEquals("floor", selector.keys(1))

  }
}
