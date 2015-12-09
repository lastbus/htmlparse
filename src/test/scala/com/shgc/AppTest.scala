package com.shgc

import java.text.SimpleDateFormat

import com.shgc.htmlparse.waterarmy.Main
import org.junit._
import Assert._

@Test
class AppTest {

  @Test
  def appTest() = assert(true)

  @Test
  def testTime ={
    val sdf = new SimpleDateFormat("yyyyMMddHHssmm")
    val time = 1000 * 89000L
    val time2 = sdf.parse("20150710161714").getTime - sdf.parse("20150710161714").getTime
    val t = if(time2 <= 0) {println("error")} else println(Main.millisecond2ManRead(time2))
    println(Main.millisecond2ManRead(time))
  }

}


