package com.shgc.htmlparse.error

import java.util

import scala.collection.mutable.ArrayBuffer

/**
 * 记录解析错误的url
 * Created by Make on 2015/11/30.
 */
object ErrorQueue {
  val errorArray = new ArrayBuffer[String]()

  def add(url: String): Unit ={
    errorArray += url
  }


}
