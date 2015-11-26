package com.shgc.htmlparseTest.util

import java.util.regex.Pattern

import com.shgc.htmlparse.util.{TimeUtil, NumExtractUtil}
import org.junit.Test

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/25.
 */
@Test
class RegexTest {

//  @Test
  def run ={
    //QQ
    var pattern = Pattern.compile("http://club\\.auto\\.qq\\.com/t-\\d+-\\d+\\.htm[l]*")
    var url = "http://club.auto.qq.com/t-49-1.htm"
    println("qq: " + pattern.matcher(url).matches())

    //so hu
    pattern = Pattern.compile("http://saa\\.auto\\.sohu\\.com/[a-z]+/thread-\\d+-\\d+\\.[s]*htm[l]*")
    url = "http://saa.auto.sohu.com/benben/thread-299501945071497-1.shtml"
    println("so-hu: " + pattern.matcher(url).matches())

    //xin lang fan ye
    pattern = Pattern.compile("http://bbs\\.auto\\.sina\\.com\\.cn/\\d+[/\\d+]*/thread-\\d+-\\d+-\\d+\\.htm[l]*")
    url = "http://bbs.auto.sina.com.cn/45/thread-5081807-1-1.html"
    var url2 = "http://bbs.auto.sina.com.cn/45/34/thread-5081807-1-1.html"
    println("xin-lang: " + pattern.matcher(url).matches() + " " + pattern.matcher(url2).matches())

    //x-car
    pattern = Pattern.compile("http://www\\.xcar\\.com\\.cn/bbs/viewthread\\.php\\?tid=\\d+[(\\&page=\\d+)]*")
    url = "http://www.xcar.com.cn/bbs/viewthread.php?tid=19837654&page=2"
    println("x-car: " + pattern.matcher(url).matches())

    //pc-auto
    pattern = Pattern.compile("http://bbs\\.pcauto\\.com\\.cn/topic-\\d+[-(\\d+)]*\\.htm[l]*")
    url = "http://bbs.pcauto.com.cn/topic-6097229-3.html"
    println("pc-auto: " + pattern.matcher(url).matches())

    //bit-auto
    pattern = Pattern.compile("http://baa\\.bitauto\\.com/\\w+/thread-\\d+[-\\d+]*.htm[l]*")
    url = "http://baa.bitauto.com/changancs75/thread-8455160.html"
    url2 = "http://baa.bitauto.com/changancs75/thread-8455160-2.html"
    println("bit-auto: " + pattern.matcher(url).matches() + " " + pattern.matcher(url2).matches())

    //auto-home
    pattern = Pattern.compile("http://club\\.autohome\\.com\\.cn/bbs/thread-[]a-z]+-\\d+-\\d+-\\d+\\.htm[l]*")
    url = "http://club.autohome.com.cn/bbs/thread-a-100001-42347470-1.html"
    url2 = "http://club.autohome.com.cn/bbs/thread-a-100001-42347470-2.html"
    println("auto-home: " + pattern.matcher(url).matches() + " " + pattern.matcher(url2).matches())
//
//    //so hu
//    pattern = Pattern.compile("")
//    url = ""
//    println("so-hu: " + pattern.matcher(url).matches())

  }

//  @Test
  def extractNum = {
    val jhj = "精华; 90贴| 80 回复| 1000 查看"
    val aa = "卡积分户籍卡健康的房间打开"
    val array = NumExtractUtil.getNumArray(aa)
    if(array.size >0) for(a <-array)println(a) else println("No number found")

  }

//  @Test
  def run2 = {
    val s = "8 (0精华)"
    for(a<- NumExtractUtil.getNumArray(s))println(a)
  }

//  @Test
  def run3 ={
    val t = "很低： jksdfjkd 112014-07-09"
        println(TimeUtil.getBitAutoTime(t))

  }




}
