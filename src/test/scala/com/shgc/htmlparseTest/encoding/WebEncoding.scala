package com.shgc.htmlparseTest.encoding

import java.net.{HttpURLConnection, URL, Socket}

import org.junit.Test


/**
 * Created by Administrator on 2015/11/27.
 */
@Test
class WebEncoding {
  val urls = Array("http://club.autohome.com.cn/bbs/thread-c-590-2884982-1.html",
                    "http://www.baidu.com/")

  @Test
  def getEncoding = {

    for(url <- urls){
      val u = new URL(url)
      val u1 = u.openConnection()
      val u2 = u1.getInputStream

      println(s"${u.getHost}  " + u1.getContentEncoding)


    }


  }
}
