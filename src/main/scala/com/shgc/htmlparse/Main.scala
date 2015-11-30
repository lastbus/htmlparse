package com.shgc.htmlparse

import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

import com.shgc.htmlparse.parse.{AutoHomeParser, ParserFactory, Parser}
import com.shgc.htmlparse.util.{Selector, ParseConfiguration, SparkManagerFactor}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.log4j.LogManager
import org.apache.nutch.protocol.Content
import org.apache.spark.SparkContext
import org.apache.spark.rdd.{PairRDDFunctions, RDD}

import scala.xml.XML


/**
 * Created by make on 2015/11/18.
 */
object Main{
  val LOG = LogManager.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit ={

    val timeStart = new Date()
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    println(s"=============#### program begins at: ${sdf.format(timeStart)} ###==============")

    run(args)
//    testHBase()

    println("\ntime taken: " + (new Date().getTime - timeStart.getTime) / 1000 + " seconds\n\n")
    System.exit(0)
  }

  def testHBase(): Unit = {
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val hadoopConf = SparkManagerFactor.getHBaseHadoopConf("hh")
    val list = 1 to 1000
    val putRDD: RDD[(ImmutableBytesWritable, Put)] = sc.parallelize(list).map(num => {
      val put = new Put(Bytes.toBytes("000" + num))
      put.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("test"), Bytes.toBytes(num))
    }).map(put => (new ImmutableBytesWritable(put.getRow), put))

    val putt = new PairRDDFunctions[ImmutableBytesWritable, Put](putRDD)
    putt.saveAsNewAPIHadoopDataset(hadoopConf)
//    putRDD.saveAsNewAPIHadoopDataset(hadoopConf)

  }

  def run(args: Array[String]): Unit ={
    if(args.length < 2 ){
      println("please input the path and hbase table name")
      System.exit(-1)
    }

    val path = args(0)
    val table = args(1)

    val parser = ParserFactory.createParseMap()
    val selector = new Selector() //just for invocation method
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)

    val rawDataRDD = sc.sequenceFile[Text, Content](path)
    println(s"input html num: ${rawDataRDD.count()}")
//    rawDataRDD.cache()

    val autoHome = Pattern.compile("http://club.autohome.com.cn/bbs/thread-[a-z]+-\\d+-\\d+-\\d+.htm[l]*")
    val autoHomeParse = new AutoHomeParser
    val b = rawDataRDD.map(doc => {
//      var put: Array[Put] = null
      if(autoHome.matcher(doc._1.toString).matches()) {
//        try{
          autoHomeParse.run(doc._2, selector)
//        }catch {
//          case _ :Exception => {println("exception:  " + doc._1); put = null}
//        }
      }else null
//      put
    })
    println(s"autohome : ${b.count()}")

//    val b = rawDataRDD.map{case(url, content) =>{
//      var pars: Parser = null
//      for((urlPattern, p) <- parser){
//        if(urlPattern.matcher(url.toString).matches()) pars = p
//      }
//
//      if(pars == null) null else {
//        pars.run(content, selector)
//      }
//    }}
    val c = b.filter(puts => puts != null)

//    rawDataRDD.unpersist()
    c.cache()
    println(s"html after filter: ${c.count()}")

    val result = c.flatMap(put => put).filter(put => put != null).map(put => (new ImmutableBytesWritable(put.getRow), put))
    result.cache()
    c.unpersist()
    println(s"floor num:  ${result.count()}")

    val hadoopConf = SparkManagerFactor.getHBaseHadoopConf(table)
    result.saveAsNewAPIHadoopDataset(hadoopConf)
    result.unpersist()


    sc.stop()
  }

  def oldRun(args: Array[String]): Unit ={

    require(ParseConfiguration.readConf())
    val paths = ParseConfiguration.hdfs2HBaseMap
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)

    val urlParserMap = ParseConfiguration.urlMap.toMap[Pattern, Selector]
    for((url, se) <- urlParserMap){
      println(url)
    }
    require(urlParserMap.size > 0)

    //    for((path, (tableName, className)) <- paths){
    val parser: Parser = ParserFactory.getParser("autohomeparser")
    if(parser == null){
      println(s"cannot find the parser: autohomeparser")
      System.exit(-1)
    }
    val data = sc.sequenceFile[Text, Content]("/user/nutch/autohome/crawl/segments/2*/content/part*/data")
    println(s"read in records:  ${data.count()}")
    val result = parser.parse(data, urlParserMap)
    result.cache()
    println(s"result records:  ${result.count()}")
    // hadoop conf
    val hadoopConf = SparkManagerFactor.getHBaseHadoopConf("hh")
    //保存结果
    result.saveAsNewAPIHadoopDataset(hadoopConf)
    result.unpersist()
    //    }


    sc.stop()
  }
}
