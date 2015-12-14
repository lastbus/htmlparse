package com.shgc

import java.util.{Date, Locale}
import java.util.{Date, Locale}
//import java.text.DateFormat
import java.text.DateFormat._

import scala.collection.mutable

/**
 * Hello world!
 *
 */
object App  {
  def main(args: Array[String]): Unit ={
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
  }


}
