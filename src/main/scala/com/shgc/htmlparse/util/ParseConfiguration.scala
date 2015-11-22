package com.shgc.htmlparse.util

import java.util.regex.Pattern

import org.apache.log4j.LogManager
import scala.collection.mutable
import scala.xml.{NodeSeq, XML}


/**
 * Created by make on 2015/11/18.
 */
object ParseConfiguration extends Serializable{

  @transient val LOG = LogManager.getLogger(this.getClass.getName)

  val urlMap = mutable.HashMap.empty[Pattern, Selector]
  val hdfs2HBaseMap = mutable.HashMap.empty[String, (String, String)]

  def  readConf(): Boolean = {
    //读取解析配置文件
    val xMLReader2 = XML.loadFile("conf.xml")
    val paths = (xMLReader2 \ "path")
    for(path <- paths) {
      val hBaseTableName = (path \ "@hbase").text
      val className = (path \ "@class").text
      hdfs2HBaseMap(path.text) = (hBaseTableName, className)
    }

    val xmlReader = XML.loadFile("parseconftest.xml")
    val urls = xmlReader \ "url"
    for(url <- urls){
      val name = (url \ "@name").text
      val encoding = (url \ "@encoding").text
      val body = (url \ "body").text
      val array = decodeSelector(url \ "selector")

      val strategy = url \ "strategy"
      val keys = url \ "key"
      val separator =  url \ "separator"

      var strategyArray: Array[Array[(String, String, String)]] = null
      if(strategy != null){
        val selectors = strategy \ "selectors"
        strategyArray = new Array[Array[(String, String, String)]](selectors.size)
        var i = 0
        for(s <- selectors){
          strategyArray(i) = decodeSelector(s \ "selector")
          i += 1
        }
      }

      var keyArray: Array[String] = null
      if(keys != null){
        keyArray = new Array[String](keys.size)
        var i = 0
        for(key <- keys){
          keyArray(i) = key.text
          i += 1
        }
      }


      if (strategyArray != null && keyArray != null)
        urlMap(Pattern.compile(name)) = new Selector(name, encoding, body, array, strategyArray, keyArray, separator.text)
      else urlMap(Pattern.compile(name)) = new Selector(name, encoding, body, array)
    }
    true
  }

  def decodeSelector(nodeSeq: NodeSeq): Array[(String, String, String)] ={
    val array = new Array[(String, String, String)](nodeSeq.length)
    var i = 0
    for(regex <- nodeSeq){
      val s = (regex \ "@value").text
      val columFamily = (regex \ "columnFamily").text
      val column = (regex \ "column").text
      array(i) = (s, columFamily, column)
      i += 1
    }
    array
  }


}
