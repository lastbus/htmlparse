package com.shgc.analysis

import java.text.SimpleDateFormat
import java.util.Date

import com.shgc.excel.ReadExcel
import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Scan, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.{Accumulator, SparkContext}
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

    val timeStart = new Date()
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    println(s"=============#### program begins at: ${sdf.format(timeStart)} ###==============")


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

    val dataBroadcast = sc.broadcast(mapArray2Map(data))
    val data2Broadcast = sc.broadcast(mapArray2Map(data2))
    val count = sc.accumulator(0)

    if (vehicleBand != null && carType != null) {
      val dataRawRDD = HBaseSparkUtil.getAllRDD(sc, tableName, "comments", "comment").map { case (url, comment) => {
        count += 1
        val array = dataBroadcast.value
        val arrayBuffer = new ArrayBuffer[(String, String, String)]
          for ((key, value) <- array) {
            var has = false
            for (s <- value if (!has)) if (comment.contains(s)) has = true
            if (has) arrayBuffer += ((url, key, comment))
          }
        if (arrayBuffer.size > 0) arrayBuffer else null
      }}.filter(_ != null).flatMap(d => d)

      val emotion: RDD[(String, String, String)] = dataRawRDD.map{case (url, key, comment) =>{
        if(key.startsWith("产品")){
          val array2 = data2Broadcast.value
          val array = array2.getOrElse(key.replace("关注点", "抱怨点"), null)
          //        var result: (String, String, String) = (null, null, null)
          var has = false
          if(array == null) {
            //抱怨点关键词没找到，开始找满意点关键词
            val satisfy = array2.getOrElse(key.replace("关注点", "满意点"), null)
            if(satisfy != null){
              //找到满意点关键词了
              for(w <- satisfy if(!has)) if(comment.contains(w)) has = true
              if(has) (url, key, Emotion.positive.toString) else (url, key, Emotion.neutral.toString)//含有满意关键词则满意，否则就是中性
            } else (url, key, Emotion.notFind.toString) //满意和抱怨的关键词条配置文件中没有找到
          } else {
            //找到抱怨点关键词
            for(word <- array if(!has)) if(comment.contains(word)) has = true
            if(has)(url, key, Emotion.negative.toString) //评论中含有抱怨点关键词
            else {
              //不含有抱怨关键词，开始找满意关键词
              val satisfy = array2.getOrElse(key.replace("关注点", "满意点"), null)
              for(w <- satisfy if(!has)) if(comment.contains(w)) has = true
              if(has) (url, key, Emotion.positive.toString) else (url, key, Emotion.neutral.toString)
            }
          }
        }else {
          (url, key, Emotion.notFind.toString)
        }
      }}
      emotion.saveAsTextFile("/user/hdfs/temp2/analyze")
    } else if(vehicleBand != null) {
      val dataRawRDD = HBaseSparkUtil.getVehicleBandRDD(sc, vehicleBand, tableName, "comments", "comment")
      println(s"${vehicleBand} number:  ${dataRawRDD.count}")
    }

    println("counter: " + count)
    sc.stop()

    println("\ntime taken: " + (new Date().getTime - timeStart.getTime) / 1000 + " seconds\n\n")
    System.exit(0)



  }

  def test(url: String, comment: String,
           array1: Map[String, Array[String]], array2: Map[String, Array[String]]): Array[(String, String, String)] = {
    val arrayBuffer = new ArrayBuffer[(String, String, String)]
    //        for (map <- array) {
    for ((key, value) <- array1) {
      var has = false
      for (s <- value if (!has)) if (s.length > 0 && comment.contains(s)) {
        has = true; println(key + " : " + s)
      }
//      var has = false
      println("==========" + key)
      val array = array2.getOrElse(key.replace("关注点", "抱怨点"), null)
      if (array == null) {
        println(key.replace("关注点", "抱怨点"))
        //抱怨点关键词没找到，开始找满意点关键词
        val satisfy = array2.getOrElse(key.replace("关注点", "满意点"), null)
        if (satisfy != null) {
          //找到满意点关键词了
          for (w <- satisfy if (!has)) if (comment.contains(w)) has = true
          if (has) arrayBuffer += ((url, key, Emotion.positive.toString)) else arrayBuffer += ((url, key, Emotion.neutral.toString)) //含有满意关键词则满意，否则就是中性
        } //else arrayBuffer += ((url, key, Emotion.notFind.toString)) //满意和抱怨的关键词条配置文件中没有找到
      } else {
        //找到抱怨点关键词
        for (word <- array if (!has)) if (comment.contains(word)) has = true
        if (has) arrayBuffer += ((url, key, Emotion.negative.toString)) //评论中含有抱怨点关键词
        else {
          //不含有抱怨关键词，开始找满意关键词
          val satisfy = array2.getOrElse(key.replace("关注点", "满意点"), null)
          for (w <- satisfy if (!has)) if (comment.contains(w)) has = true
          if (has) arrayBuffer += ((url, key, Emotion.positive.toString)) else arrayBuffer += ((url, key, Emotion.neutral.toString))
        }
      }
    }
    arrayBuffer.toArray
  }

  def oneFunction(url: String, comment: String,
                  array: Map[String, Array[String]], array2: Map[String, Array[String]]): Array[(String, String, String)] ={
    val arrayBuffer = new ArrayBuffer[(String, String, String)]
    //        for (map <- array) {
    for ((key, value) <- array) {
      var has = false
      for (s <- value if (!has)) if ( s.length > 0 && comment.contains(s)) {has = true; println(key+" : " + s)}

      if (has && key.startsWith("产品")) {
        //找抱怨点关键词，然后是满意点，都找不到则没有
        val complain = array2.getOrElse(key.replace("关注点", "抱怨点"), null)
        if(complain == null) {// 没找到抱怨点的关键词字段
        //找满意点
        val satisfy = array2.getOrElse(key.replace("关注点", "满意点"), null)
          if(satisfy == null) arrayBuffer += ((url, key, Emotion.notFind.toString)) //没找到满意点关键词
          else {
            //找到满意点关键词，满意或者中立
            var has2 = false
            for( word <- satisfy if(!has2)) if(comment.contains(word)) has2 = true
            if(has2) arrayBuffer += ((url, key, Emotion.positive.toString))
            else arrayBuffer += ((url, key, Emotion.neutral.toString))
          }
        } else {
          //找到抱怨点关键词, 抱怨、满意、或中立
          var has2 = false
          for(word <- complain if(!has2))if(comment.contains(word)) has2 = true
          if(has2) arrayBuffer += ((url, key, Emotion.negative.toString)) else {
            //抱怨关键点没找到，开始找满意点
            val satisfy = array2.getOrElse(key.replace("关注点", "满意点"), null)
            if(satisfy == null) arrayBuffer += ((url, key, Emotion.neutral.toString)) //没找到满意点关键词
            else {
              //找到满意点关键词，满意或者中立
              var has2 = false
              for( word <- satisfy if(!has2)) if(comment.contains(word)) has2 = true
              if(has2) arrayBuffer += ((url, key, Emotion.positive.toString))
              else arrayBuffer += ((url, key, Emotion.neutral.toString))
            }
          }
        }
      }else if(has){
        arrayBuffer += ((url, key, Emotion.notFind.toString))
      }
    }
    //        }
    if (arrayBuffer.size > 0) arrayBuffer.toArray else null
  }


  def mapArray2Map(mapArray: Array[Map[String, Array[String]]]): Map[String, Array[String]] = {
    if(mapArray == null || mapArray.size == 0) return null
    val map = scala.collection.mutable.HashMap.empty[String, Array[String]]
    for(m <- mapArray){
      for((key, value) <- m){
        if(map.contains(key)) println("there are conflits in hash map")
        map(key) = value
      }
    }
    map.toMap
  }

  def analysis(hBaseTimeIntervalTest: RDD[(String, String)], dataBroadcast: Broadcast[Array[Map[String, Array[String]]]],
                data2Broadcast: Broadcast[Array[Map[String, Array[String]]]],
               counter: Accumulator[Int]): Unit = {
    val a = hBaseTimeIntervalTest.map { case (url, comment) => {
      counter += 1
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
    }}.filter(_ != null).flatMap(d => d)
//    a.cache()
    a.saveAsTextFile("/user/hdfs/temp2/analyze")

//    val temp = a.map { case (url, feature, num) => (feature, num) }.reduceByKey((f1, f2) => f1 + f2)
//
//    temp.cache()
//    temp.filter(d => d._1.startsWith("产品") && d._1.contains("关注点")).coalesce(1).map(d => d._1 + "," + d._2).
//      saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin")
//    temp.filter(d => d._1.startsWith("品牌")).coalesce(1).map(d => d._1 + "," + d._2).
//      saveAsTextFile("/user/hdfs/temp2/analyzeSum/pinpai")
//    temp.filter(d => d._1.startsWith("售后服务") || d._1.startsWith("销售服务")).coalesce(1).map(d => d._1 + "," + d._2).
//      saveAsTextFile("/user/hdfs/temp2/analyzeSum/fuwu")
//    temp.unpersist()
//    a.unpersist()
//
//    val emotionRDD = hBaseTimeIntervalTest.map { case (url, comment) => {
//      val array = dataBroadcast.value
//      val arrayBuffer = new ArrayBuffer[(String, String, String)]
//      for (map <- array) {
//        for ((key, value) <- map) {
//          var has = false
//          for (s <- value if (!has)) if (comment.contains(s)) has = true
//          if (has) arrayBuffer += ((url, key, comment))
//        }
//      }
//      if (arrayBuffer.size > 0) arrayBuffer else null
//    }
//    }.filter(_ != null).flatMap(d => d).
//      filter(d => d._2.contains("产品") && d._2.contains("关注点")).
//      map { case (url, key, comment) => {
//        val array = data2Broadcast.value
//        val result = new Array[(String, String, Int)](2)
//        val satisfy = array(0)
//        val value = satisfy.getOrElse(key.replace("关注点", "满意点"), null)
//        result(0) = if (value == null) null
//        else {
//          var has = false
//          for (s <- value if (!has)) if (comment.contains(s)) has = true
//          if (has) (url, key.replace("关注点", "满意点"), 1) else null
//        }
//        val complain = array(1).getOrElse(key.replace("关注点", "抱怨点"), null)
//        result(1) = if (complain == null) null
//        else {
//          var has = false
//          for (s <- complain if (!has)) if (comment.contains(s)) has = true
//          if (has) (url, key.replace("关注点", "抱怨点"), 1) else null
//        }
//        result
//      }
//      }.flatMap(d => d).filter(_ != null).
//      map { case (url, feature, num) => (feature, num) }.reduceByKey((f1, f2) => f1 + f2)
//    emotionRDD.cache()
//    println(emotionRDD.count)
//    emotionRDD.coalesce(1).map(d => d._1 + "," + d._2).saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin-")
//    emotionRDD.filter(d => d._1.contains("满意点")).coalesce(1).map(d => d._1 + "," + d._2).
//      saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin-manyi")
//    emotionRDD.filter(d => d._1.contains("抱怨点")).coalesce(1).map(d => d._1 + "," + d._2).
//      saveAsTextFile("/user/hdfs/temp2/analyzeSum/chanpin-baoyuan")
//    emotionRDD.unpersist()
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
