package com.shgc.htmlparse.util

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.mapreduce.{TableOutputFormat, TableInputFormat}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.{SparkConf, SparkContext}


/**
 * Created by make on 11/8/15.
 */
object SparkManagerFactor {
  private[this] var sc: SparkContext = null
  private[this] var hBaseConf: Configuration = null
  private[this] var table: Table = null
  private[this] var connection: Connection = null
  private[this] var hadoopConf: Configuration = null

  def getSparkContext(appName: String = this.getClass.getName): SparkContext = {
    if(sc != null) sc else {
      val sparkConf = new SparkConf().setAppName(appName).
        set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
//      sparkConf.setMaster("local[3]")
      sc = new SparkContext(sparkConf)
      sc
    }
  }

  def getHBaseConf(): Configuration ={
    if(hBaseConf != null) hBaseConf else{
      hBaseConf = HBaseConfiguration.create()
      hBaseConf.addResource("hbase-site.xml")
      hBaseConf
    }
  }

  def getHBaseConf(tableName: String, connType: String): Configuration ={
    val check = connType.equals(TableInputFormat.INPUT_TABLE) ||
                connType.equals(TableOutputFormat.OUTPUT_TABLE)
    if(check){
      if (hBaseConf == null) {
        hBaseConf = HBaseConfiguration.create()
        hBaseConf.addResource("hbase-site.xml")
        hBaseConf.set(connType, tableName)
        hBaseConf
      } else if (hBaseConf.get(connType).equals(tableName)) hBaseConf
      else{
        hBaseConf.set(connType, tableName)
        hBaseConf
      }
    }else{
      null
    }
  }
  def getHBaseHadoopConf2(tableName: String): Configuration = {
    if(sc == null) getSparkContext()
    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    val job = Job.getInstance(sc.hadoopConfiguration)
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Result])
    job.setOutputFormatClass(classOf[TableOutputFormat[(ImmutableBytesWritable, Put)]])
    hadoopConf = job.getConfiguration
    hadoopConf
  }

  def getHBaseHadoopConf(tableName: String): Configuration ={
    if(hBaseConf == null){
      hBaseConf = HBaseConfiguration.create()
//      hBaseConf.addResource("hbase-site.xml")
      if(sc == null) getSparkContext()
      sc.hadoopConfiguration.set("hbase.zookeeper.quorum ","shgc02,shgc03,shgc04")
      sc.hadoopConfiguration.set("zookeeper.znode.parent","/hbase")
      sc.hadoopConfiguration.set(TableOutputFormat.OUTPUT_TABLE, tableName)
//      val job = new Job(hBaseConf)
      val job = Job.getInstance(sc.hadoopConfiguration, tableName)
      job.setOutputKeyClass(classOf[ImmutableBytesWritable])
      job.setOutputValueClass(classOf[Result])
      job.setOutputFormatClass(classOf[TableOutputFormat[(ImmutableBytesWritable, Put)]])
      hadoopConf = job.getConfiguration
      hadoopConf
    }else if(!hBaseConf.get(TableOutputFormat.OUTPUT_TABLE).equals(tableName)) {
      val job = Job.getInstance(hBaseConf, "hadoopConf" + tableName)
      job.setOutputKeyClass(classOf[ImmutableBytesWritable])
      job.setOutputValueClass(classOf[Result])
      job.setOutputFormatClass(classOf[TableOutputFormat[(ImmutableBytesWritable, Put)]])
      hadoopConf = job.getConfiguration
      hadoopConf
    } else hadoopConf
  }

  def getHBaseConnection(tableName: String): Table = {
    if (table == null){
      val connection = getHBaseConnection()
      table = connection.getTable(TableName.valueOf("qy58"))
      table
    }else table
  }

  private def getHBaseConnection(): Connection ={
    if (connection == null) ConnectionFactory.createConnection(hBaseConf) else connection
  }
}
