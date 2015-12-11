package com.shgc.excelTest

import org.junit.Test

/**
 * Created by Administrator on 2015/12/11.
 */
@Test
class SplitTest {

  @Test
  def split = {
    val a = "ab,cd.efg/kk;hi；jklm"
    val regex = "[,./;；]"
    a.split(regex).foreach(println)
  }
}
