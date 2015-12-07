//package com.shgc.htmlparse.t0.predict
//
//import com.shgc.htmlparse.t0.LoadData
//import com.shgc.htmlparse.util.SparkManagerFactor
//import org.apache.hadoop.hbase.client.Result
//import org.apache.hadoop.hbase.io.ImmutableBytesWritable
//import org.apache.hadoop.hbase.mapreduce.TableInputFormat
//import org.apache.hadoop.hbase.util.Bytes
//import org.apache.spark.rdd.RDD
//
//import scala.collection.mutable.ArrayBuffer
//
///**
// * Created by Administrator on 2015/12/3.
// */
//class LoadHBaseData extends LoadData{
//  val colums = Array(Bytes.toBytes("comment"), Bytes.toBytes("company"))
////  val columnFamily = Bytes.toBytes("comments")
//
//  override def getData[k, v](): RDD[(String, Array[String])] = {
//    val sc = SparkManagerFactor.getSparkContext("")
//    val conf = SparkManagerFactor.getHBaseConf()
//    val rawDataRDD = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result]).
//      map{case (key, value) => {
//      val key = new String(value.getRow)
//      val array = for(col <- colums) yield new String(value.getValue(columnFamily, col))
//      (key, array)
//    }}
//    rawDataRDD
//  }
////  override def getData[k, v](): RDD[(k, v)] = ???
//}
