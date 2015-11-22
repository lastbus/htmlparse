package com.shgc.htmlparse.util

import java.util.regex.Pattern

/**
 * Created by make on 2015/11/18.
 */
class Selector extends Serializable{
  var url: String = null
  var urlPattern: Pattern = null
  var encoding: String = null
  var body: String = null
  var select: Array[(String, String, String)] = null

  var strategySelector: Array[Array[(String, String, String)]] = null
  var keys: Array[String] = null
  var separator: String = null

  def this(url: String, encoding: String, body: String, select: Array[(String, String, String)]) = {
    this()
    this.url = url
    this.urlPattern = Pattern.compile(url)
    this.encoding = encoding
    this.body = body
    this.select = select
  }

  def this(url: String, encoding: String, body: String, select: Array[(String, String, String)],
           strategy: Array[Array[(String, String, String)]]) = {
    this()
    this.url = url
    this.urlPattern = Pattern.compile(url)
    this.encoding = encoding
    this.body = body
    this.select = select
    this.strategySelector = strategySelector
  }


  def this(url: String, encoding: String, body: String, select: Array[(String, String, String)],
           strategy: Array[Array[(String, String, String)]], keys: Array[String], separator: String) = {
    this()
    this.url = url
    this.urlPattern = Pattern.compile(url)
    this.encoding = encoding
    this.body = body
    this.select = select
    this.strategySelector = strategySelector
    this.keys = keys
    this.separator = separator
  }

}
