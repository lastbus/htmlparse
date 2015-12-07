package com.shgc.htmlparse.waterarmy

import java.text.SimpleDateFormat

import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable


/**
 * Created by make on 2015/12/4.
 */
object Main {

  val columnFamily = Bytes.toBytes("comments")
  val columns = Array("username", "registertime", "publish", "response", "posttime")
  val columnsBytes= for(column <- columns) yield Bytes.toBytes(column)

  def main(args: Array[String]): Unit ={


    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val hBaseConf = SparkManagerFactor.getHBaseConf("hh", TableInputFormat.INPUT_TABLE)
//    val rawDataRDD = getRawStatistic(sc, hBaseConf)
    val rawDataRDD = getAverageIntervalPost(sc, hBaseConf)
    rawDataRDD.saveAsTextFile("/user/hdfs/temp2/waterarmy")
  }

  def getRawStatistic(sc: SparkContext, hBaseConf: Configuration): RDD[(String, (String, String, String, String))] ={
    val rawDataRDD = sc.newAPIHadoopRDD(hBaseConf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result]).
      map(d =>{
        val content = d._2
        val key = new String(content.getRow)
        val array = new Array[String](columnsBytes.length)
        var temp: Array[Byte] = null
        for(i <- 0 until array.length){
          temp = content.getValue(columnFamily, columnsBytes(i))
          if(temp != null && temp.length > 0) array(i) = new String(temp) else array(i) = "-1"
        }
        (array(0), (array(1), array(2), array(3), key))
      }).
      reduceByKey((s1, s2) => if(s1._3.toInt > s2._3.toInt) s1 else s2).
      sortByKey()
    rawDataRDD
  }

  def getAverageIntervalPost(sc: SparkContext, hBaseConf: Configuration):
                  RDD[(String, String, String, String, String, String, Int, String)] ={
    //依次为 注册时间、发帖数量、回复数量、第一次发帖时间、最后发帖时间、发言总次数、 最后一次发帖URL
    val initialValue = ("", "-9999", "-9999","999999999", "00000000000", 0)
    val addToValue = (a: (String, String, String, String, String, Int) ,
                      b: (String, String, String, String, String)) => {
//      val c0 = b._1
      val c1 = if(a._2.toInt < b._2.toInt) b._2 else a._2
      val c2 = if(a._3.toInt < b._3.toInt) b._3 else b._3
      val c3 = if(a._4 > b._4) b._4 else a._4
      val c4 = if(a._5 < b._4) b._4 else a._5
      val c5 = a._6 + 1
      (b._1, c1, c2, c3, c4, c5)
    }

    val mergePartition = (a1: (String, String, String, String, String, Int),
                          a2:  (String, String, String, String, String, Int)) =>{
      val c1 = if(a1._2 < a2._2) a2._2 else a1._2
      val c2 = if(a1._3 < a2._3) a2._3 else a1._3
      val c3 = if(a1._4 > a2._4) a2._4 else a1._4
      val c4 = if(a1._5 < a2._4) a2._4 else a1._5
      val c5 = a1._6 + a2._6
      (a1._1, c1, c2, c3, c4, c5)
    }

    val rawDataRDD = sc.newAPIHadoopRDD(hBaseConf,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result]).
      map(d =>{
        val content = d._2
        val key = new String(content.getRow)
        val array = new Array[String](columnsBytes.length)
        var temp: Array[Byte] = null
        for(i <- 0 until array.length){
          temp = content.getValue(columnFamily, columnsBytes(i))
          if(temp != null && temp.length > 0) array(i) = new String(temp) else array(i) = "-1"
        }
        (array(0), (array(1), array(2), array(3), array(4), key))
      }).aggregateByKey(initialValue)(addToValue, mergePartition).
      map(user => {
        val sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        val millisecond = sdf.parse(user._2._5).getTime - sdf.parse(user._2._4).getTime
        val forumCount = Math.max(user._2._2.toInt, user._2._3.toInt)
        val timeInterval = if(forumCount <= 0 || millisecond == 0) "error" else millisecond2ManRead(millisecond / forumCount)
        (user._1, user._2._1, user._2._2, user._2._3, user._2._4, user._2._5, user._2._6, timeInterval)
      }).sortBy(_._1)
    rawDataRDD
    }

  def test(sc: SparkContext): Unit ={
    val keysWithValuesList = Array("foo=A", "foo=A", "foo=A", "foo=A", "foo=B", "bar=C", "bar=D", "bar=D")
    val data = sc.parallelize(keysWithValuesList)
    //Create key value pairs
    val kv = data.map(_.split("=")).map(v => (v(0), v(1))).cache()
    val initialSet = mutable.HashSet.empty[String]
    val addToSet = (s: mutable.HashSet[String], v: String) => s += v
    val mergePartitionSets = (p1: mutable.HashSet[String], p2: mutable.HashSet[String]) => p1 ++= p2
    val uniqueByKey = kv.aggregateByKey(initialSet)(addToSet, mergePartitionSets)
    uniqueByKey.collect().foreach(println)
  }

  def millisecond2ManRead(time: Long): String ={
    val day = time / (24 * 3600 * 1000)
    val hour = time % (24 * 3600 * 1000) / (3600 * 1000)
    val minute = time % (24 * 3600 * 1000) % (3600 * 1000) / (60 * 1000)
    val second = time % (24 * 3600 * 1000) % (3600 * 1000) % (60 * 1000) / 1000
    day + " 天 " + hour + ":" + minute+ ":" + second
  }

}
