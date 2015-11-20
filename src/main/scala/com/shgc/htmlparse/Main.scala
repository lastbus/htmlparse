package com.shgc.htmlparse

import java.text.SimpleDateFormat
import java.util.Date

import com.shgc.htmlparse.parse.{ParserFactory}
import com.shgc.htmlparse.util.{ParseConfiguration, SparkManagerFactor}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.log4j.LogManager
import org.apache.nutch.protocol.Content
import com.shgc.htmlparse.parse.Parser

import scala.xml.XML


/**
 * Created by make on 2015/11/18.
 */
object Main {
  val LOG = LogManager.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit ={

    val timeStart = new Date()
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    println(s"=============#### program begins at: ${sdf.format(timeStart)} ###==============")

    require(ParseConfiguration.readConf())
    val paths = ParseConfiguration.hdfs2HBaseMap
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)

    val urlParserMap = ParseConfiguration.urlMap.toMap
    require(urlParserMap.size > 0)

    for((path, (tableName, className)) <- paths){
      val parser: Parser = ParserFactory.getParser(className)
      if(parser == null){
        println(s"cannot find the parser: ${className}")
        System.exit(-1)
      }
      val data = sc.sequenceFile[Text, Content](path)
      println(s"read in records:  ${data.count()}")
      val result = parser.parse(data, urlParserMap)
      result.cache()
      println(s"result records:  ${result.count()}")
      // hadoop conf
      val hadoopConf = SparkManagerFactor.getHBaseHadoopConf(tableName)
      //保存结果
      result.saveAsNewAPIHadoopDataset(hadoopConf)
      result.unpersist()
    }


    sc.stop()
    println("\ntime taken: " + (new Date().getTime - timeStart.getTime) / 1000 + " seconds\n\n")
    System.exit(0)
  }

}
