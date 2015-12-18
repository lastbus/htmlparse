package com.shgc.htmlparse

import com.shgc.htmlparse.parse.{Parser, ParserFactory}
import com.shgc.htmlparse.util.{SparkManagerFactor, Selector}
import com.shgc.proxy.Task
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.io.Text
import org.apache.nutch.protocol.Content

/**
 * Created by make on 2015/12/18.
 */
class ParseTask extends Task{

  override def run(args: Array[String]): Int = {
    if(args.length < 2 ){
      println("please input the path and hBase table name")
      return -1
    }
    val path = args(0)
    val table = args(1)
    val parser = ParserFactory.createParseMap()
    val selector = new Selector() //just for invocation method
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val rawDataRDD = sc.sequenceFile[Text, Content](path)
    println(s"input html num: ${rawDataRDD.count()}")

    val b = rawDataRDD.map{case(url, content) =>{
      var pars: Parser = null
      for((urlPattern, p) <- parser){
        if(urlPattern.matcher(url.toString).matches()) pars = p
      }

      if(pars == null) null else {
        try{
          pars.run(content, selector)
        }catch {
          case _ : Exception => {println("error: " + url.toString); null}
        }
      }
    }}
    val c = b.filter(puts => puts != null)
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
}
