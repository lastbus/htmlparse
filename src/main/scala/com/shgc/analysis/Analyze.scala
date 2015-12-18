package com.shgc.analysis

import com.shgc.excel.ReadExcel
import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Scan, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

import scala.Predef
import scala.collection.mutable.ArrayBuffer

/**
 *
 * 输入参数：
 * Created by make on 2015/12/9.
 */
object Analyze {

  val columnFamily = Bytes.toBytes("comments")
  val columnsBytes= Bytes.toBytes("comment")

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      println("please input table name and file-path")
      System.exit(-1)
    }

    val names = Array(("产品关注点", 3), ("服务关注点", 2), ("品牌关注点", 3))
    val names2 = Array(("车主对产品的满意点", 3), ("车主对产品的抱怨点", 3))

    val readExcel = new ReadExcel
    val tableName = args(0)
    val data = readExcel.read(args(1), names)
    val data2 = readExcel.read(args(1), names2)

    val vehicleBand = if (args.length > 2) args(2) else null
    val carType = if (args.length > 3) args(3) else null
    val timeInterval = if (args.length > 4) args(4) else null

    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val dataBroadcast = sc.broadcast(data)
    val data2Broadcast = sc.broadcast(data2)

    if (vehicleBand != null && carType != null) {
      val dataRawRDD = HBaseSparkUtil.getCarTypeRDD(sc, vehicleBand, carType, tableName, "comments", "comment")
      println(s"${vehicleBand}  ${carType} number:  ${dataRawRDD.count()}")
//      analysis(dataRawRDD, dataBroadcast, data2Broadcast)
    } else if(vehicleBand != null) {
      val dataRawRDD = HBaseSparkUtil.getVehicleBandRDD(sc, vehicleBand, tableName, "comments", "comment")
      println(s"${vehicleBand} number:  ${dataRawRDD.count}")
//      analysis(dataRawRDD, dataBroadcast, data2Broadcast)
    }
//    val hBaseScanTest = HBaseSparkUtil.getWebsiteRDD(sc, "autohome", "hh", "comments", "comment")
//    println(s"autohome:  " + hBaseScanTest.count())
//
//    val hBaseCarTypeTest = HBaseSparkUtil.getCarTypeRDD(sc, "奔奔", "hh", "comments", "comment")
//    println(s"奔奔:  " + hBaseCarTypeTest.count())
//
//    val hBaseTimeIntervalTest = HBaseSparkUtil.getTimeInterval(sc, ".*\\|2015\\d{10}\\|.*", tableName, "comments", "comment")
//    println(s"2015:  ${hBaseTimeIntervalTest.count()}")

//    val hBaseSelect = HBaseSparkUtil.select(sc, "hh", "comments", "comment", carType = "奔奔", website = "autohome", timeIntervalRegex = ".*2015.*")
//        println("select: " + hBaseSelect.count())


    sc.stop()



  }


  def analysis(hBaseTimeIntervalTest: RDD[(String, String)], dataBroadcast: Broadcast[Array[Map[String, Array[String]]]],
                data2Broadcast: Broadcast[Array[Map[String, Array[String]]]]): Unit = {
    val a = hBaseTimeIntervalTest.map { case (url, comment) => {
      val array = dataBroadcast.value
      val arrayBuffer = new ArrayBuffer[(String, String, Int)]
      for (map <- array) {
        for ((key, value) <- map) {
          var has = false
          for (s <- value if (!has)) if (comment.contains(s)) has = true
          if (has) arrayBuffer += ((url, key, 1))
        }
      }
      if (arrayBuffer.size > 0) arrayBuffer else null
    }
    }.filter(_ != null).flatMap(d => d)
    a.cache()
    a.saveAsTextFile("/user/hdfs/temp2/analyze")

    val temp = a.map { case (url, feature, num) => (feature, num) }.reduceByKey((f1, f2) => f1 + f2)

    temp.cache()
    temp.filter(d => d._1.startsWith("产品") && d._1.contains("关注点")).coalesce(1).map(d => d._1 + "," + d._2).
      saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin")
    temp.filter(d => d._1.startsWith("品牌")).coalesce(1).map(d => d._1 + "," + d._2).
      saveAsTextFile("/user/hdfs/temp2/analyzeSum/pinpai")
    temp.filter(d => d._1.startsWith("售后服务") || d._1.startsWith("销售服务")).coalesce(1).map(d => d._1 + "," + d._2).
      saveAsTextFile("/user/hdfs/temp2/analyzeSum/fuwu")
    temp.unpersist()
    a.unpersist()

    val emotionRDD = hBaseTimeIntervalTest.map { case (url, comment) => {
      val array = dataBroadcast.value
      val arrayBuffer = new ArrayBuffer[(String, String, String)]
      for (map <- array) {
        for ((key, value) <- map) {
          var has = false
          for (s <- value if (!has)) if (comment.contains(s)) has = true
          if (has) arrayBuffer += ((url, key, comment))
        }
      }
      if (arrayBuffer.size > 0) arrayBuffer else null
    }
    }.filter(_ != null).flatMap(d => d).
      filter(d => d._2.contains("产品") && d._2.contains("关注点")).
      map { case (url, key, comment) => {
        val array = data2Broadcast.value
        val result = new Array[(String, String, Int)](2)
        val satisfy = array(0)
        val value = satisfy.getOrElse(key.replace("关注点", "满意点"), null)
        result(0) = if (value == null) null
        else {
          var has = false
          for (s <- value if (!has)) if (comment.contains(s)) has = true
          if (has) (url, key.replace("关注点", "满意点"), 1) else null
        }
        val complain = array(1).getOrElse(key.replace("关注点", "抱怨点"), null)
        result(1) = if (complain == null) null
        else {
          var has = false
          for (s <- complain if (!has)) if (comment.contains(s)) has = true
          if (has) (url, key.replace("关注点", "抱怨点"), 1) else null
        }
        result
      }
      }.flatMap(d => d).filter(_ != null).
      map { case (url, feature, num) => (feature, num) }.reduceByKey((f1, f2) => f1 + f2)
    emotionRDD.cache()
    println(emotionRDD.count)
    emotionRDD.coalesce(1).map(d => d._1 + "," + d._2).saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin-")
    emotionRDD.filter(d => d._1.contains("满意点")).coalesce(1).map(d => d._1 + "," + d._2).
      saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin-manyi")
    emotionRDD.filter(d => d._1.contains("抱怨点")).coalesce(1).map(d => d._1 + "," + d._2).
      saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin-baoyuan")
    emotionRDD.unpersist()
  }



  def test(sc: SparkContext): Unit = {

    val readString = "通过性\n底盘高;底盘够高\n离地间隙;离地\n刮底 \n地面不平 \n通过能力" +
      " \n车底 \n底盘低;底盘太低\n通过障碍物;障碍物 \n底盘设计较高 \n底盘有点低 \n适应各种路况 \n拖底"
    val testArray: Array[Array[String]] = readString.split("\n").map(_.split(";"))

    val hBaseConf = SparkManagerFactor.getHBaseConf("hh", TableInputFormat.INPUT_TABLE)
    val rawDataRDD = sc.newAPIHadoopRDD(hBaseConf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result]).
      map { case (key, content) => {
        val comment = content.getValue(columnFamily, columnsBytes)
        val r = if (comment != null) new Predef.String(comment) else null
        r
      }
      }.filter(_ != null)

    val initValue: Array[Int] = for (tt <- testArray) yield 0
    //    initValue.foreach(s => s.foreach(ss => println(s"${ss(0)} ${ss(1)} ")))
    val addToValue = (arr: Array[Int], comment: String) => {
      var i = 0
      for (words <- testArray) {
        for (word <- words) {
          if (comment.contains(word)) arr(i) += 1
        }
        i += 1
      }
      arr
    }
    val mergeFun = (arr1: Array[Int], arr2: Array[Int]) => {
      val arr0 = new Array[Int](arr1.length)
      for (i <- 0 until arr0.length) arr0(i) = arr1(i) + arr2(i)
      arr0
    }

    val result: Array[Int] = rawDataRDD.aggregate(initValue)(addToValue, mergeFun)
    result.foreach(println)

    for (i <- 0 until testArray.length) {
      for (w <- testArray(i)) print(w + ";")
      println(result(i))
    }
  }
}
