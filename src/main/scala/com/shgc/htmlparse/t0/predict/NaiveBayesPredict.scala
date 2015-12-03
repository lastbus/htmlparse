package com.shgc.htmlparse.t0.predict

import java.util
import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Scan, Get, ConnectionFactory}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vector
/**
 * Created by Administrator on 2015/12/3.
 */
class NaiveBayesPredict extends Predict{
  val model = ""
  val columnFamily = Bytes.toBytes("comments")
  val columns = Array(Bytes.toBytes("comment"), Bytes.toBytes("industry"), Bytes.toBytes("company"))

  override def run[k, v1, v2](rdd: RDD[(k, Vector)]): RDD[(k, Double)] = {
    val sc = SparkManagerFactor.getSparkContext("g")
    val model = NaiveBayesModel.load(sc, model)
//    val conf = SparkManagerFactor.getHBaseConf()
//    val connection = ConnectionFactory.createConnection(conf)
//    val table = connection.getTable(TableName.valueOf("qy58"))
//    val scan = new Scan()
//    for(column <- columns) scan.addColumn(columnFamily, column)
//    val scanner = table.getScanner(scan)


    val urls = rdd.map{case(url, value) =>{(url, model.predict(value))}}.filter(_._2 > 0.0).map(_._1.toString).collect()
//    val result = r.map()
    val result = getFullInformation(urls)
    sc.parallelize(result)
  }

  def getFullInformation(urls: Array[String]): Array[(String,Array[String])] ={
    val sc = SparkManagerFactor.getSparkContext("")
    val conf = SparkManagerFactor.getHBaseConf()
    val connection = ConnectionFactory.createConnection(conf)

    val table = connection.getTable(TableName.valueOf("qy58"))
//    val gets = new Array[Get](urls.length)
    val gets: Array[Get] = for(url <- urls) yield {
      val get = new Get(Bytes.toBytes(url))
      for(column <- columns) get.addColumn(columnFamily, column)
      get
    }

    val result = table.get(gets)
    val rr: Array[(String, Array[String])] = for(r <- result)
                                      yield {
                                        val values = for(column <- columns) yield new String(r.getValue(columnFamily, column));
                                        (new String(r.getRow), values)
                                      }
    rr
  }






  implicit def array2JavaList(array: Array[Get]): java.util.List[Get] ={
    val list = new util.ArrayList[Get]
    for(arr <- array) list.add(arr)
    list
  }
}
