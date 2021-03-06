package com.shgc

import java.util.{Date, Locale}
import java.util.{Date, Locale}

import com.shgc.analysis.Emotion

//import java.text.DateFormat
import java.text.DateFormat._

import scala.collection.mutable

/**
 * Hello world!
 *
 */
object App  {
  def main(args: Array[String]): Unit ={
    println(Emotion.positive.toString)

    println( "Hello World!" )
    // Make a list via the companion object factory
    val sortedSet = mutable.SortedSet("20150131","20151201","20141231","20131231", "20080101")
    sortedSet.foreach(println)

    val now = new Date
    val df = getDateInstance(LONG, Locale.CHINA)
    println(df format now)

    val complex = new Complex(1,3)
    println(s"real: ${complex.real}, imaginary: ${complex.imaginary}")

    val s = "123456"
    println(s.substring(s.length - 3))
    val hh = "autohome|众泰T600##|20141021071709|http://club.autohome.com.cn/bbs/thread-c-2334-34779473-1.html|0"
    println(hh.split("\\|")(1))

  }


}
