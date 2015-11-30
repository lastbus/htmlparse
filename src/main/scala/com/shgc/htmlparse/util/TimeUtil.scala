package com.shgc.htmlparse.util

import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * Created by Administrator on 2015/11/24.
 */
object TimeUtil {

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val sdf2 = new SimpleDateFormat("yyyyMMddHHmmss")
  val sdf3 = new SimpleDateFormat("yyyy年MM月dd日")
  val sdf33 = new SimpleDateFormat("yyyy-MM-dd")
  val sdf4 = new SimpleDateFormat("yyyyMMdd")
  val autoHomeTimePattern = Pattern.compile("\\d{2,4}年\\d{1,2}月\\d{1,2}日")
  val bitAuto = Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}")
  val postTimePattern = Pattern.compile("[0-9]{2,4}[--]\\d{1,2}[--]\\d{1,2}[ ]+\\d{1,2}[:：]\\d{1,2}([:：]\\d{1,2})?")

  def getAutoHomeRT(s: String): String ={
    if(s == null) return null
    val matcher = autoHomeTimePattern.matcher(s)
    if(matcher.find()) sdf4.format(sdf3.parse(matcher.group())) else null
  }


  def getFloorTime1(s: String): String ={
    if (s == null || s.length < 10) return null
    sdf2.format(sdf.parse(s))
  }

  def getPostTime(s: String): String ={
    val timeString = extractTimeString(s)
    if(timeString == null) null else sdf2.format(sdf.parse(timeString))
  }

  def extractTimeString(s: String): String = {
    if(s == null || s.length < 6) return null
    val matcher = postTimePattern.matcher(s)
    if(matcher.find()) matcher.group() else null
  }

  def getBitAutoTime(s: String): String ={
    if (s == null) return null
    val matcher = bitAuto.matcher(s)
    if(matcher.find()) sdf4.format(sdf33.parse(matcher.group())) else null
  }

}
