package com.shgc.htmlparseTest

import java.net.URL

import org.junit.Test

/**
 * Created by Administrator on 2015/11/18.
 */
@Test
class MainTest {

  @Test
  def main ={
    val url = "http://club.autohome.com.cn/bbs/forum-c-2546-1.html"
    val u = new URL(url)
    println(u.getHost)
    println(u.getHost.length)
  }

}
