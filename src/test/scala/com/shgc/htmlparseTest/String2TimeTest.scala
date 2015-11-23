package com.shgc.htmlparseTest

import java.text.SimpleDateFormat

/**
 * Created by Administrator on 2015/11/23.
 */
object String2TimeTest {
  def main(args: Array[String]): Unit ={
    val timeString = "2015-6-12 23:00:34"
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
    val time = sdf.parse(timeString)
//    println(sdf.format(timeString))

    println(time)
    println(sdf2.format(time))
  }

}
