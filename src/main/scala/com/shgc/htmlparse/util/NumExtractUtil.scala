package com.shgc.htmlparse.util

import java.util.regex.Pattern

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/11/25.
 */
object NumExtractUtil {
  val pattern = Pattern.compile("\\d+")
  val stringPattern = Pattern.compile("[a-zA-z]+")

  def getNumArray(s: String): Array[String] = {
    val matcher = pattern.matcher(s)
    val numArrayBuffer = new ArrayBuffer[String]
    while(matcher.find()){
      numArrayBuffer += matcher.group()
    }
   numArrayBuffer.toArray
  }


  def getStringArray(s: String): Array[String] ={
    val matcher = stringPattern.matcher(s)
    val stringArrayBuffer = new ArrayBuffer[String]
    while (matcher.find()){
      stringArrayBuffer += matcher.group()
    }
    stringArrayBuffer.toArray
  }

}
