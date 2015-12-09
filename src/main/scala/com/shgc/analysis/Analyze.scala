package com.shgc.analysis

import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes

/**
 *
 * 输入参数： Array[Array[String]]
 * Created by make on 2015/12/9.
 */
object Analyze {

  val columnFamily = Bytes.toBytes("comments")
  val columnsBytes= Bytes.toBytes("comment")

  def main(args: Array[String]): Unit ={
    val readString = "通过性\n底盘高;底盘够高\n离地间隙;离地\n刮底 \n地面不平 \n通过能力" +
      " \n车底 \n底盘低;底盘太低\n通过障碍物;障碍物 \n底盘设计较高 \n底盘有点低 \n适应各种路况 \n拖底"
    val testArray: Array[Array[String]] = readString.split("\n").map(_.split(";"))

    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val hBaseConf = SparkManagerFactor.getHBaseConf("hh", TableInputFormat.INPUT_TABLE)
    val rawDataRDD = sc.newAPIHadoopRDD(hBaseConf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result]).
                        map{case (key, content) =>{
                          val comment = content.getValue(columnFamily, columnsBytes)
                          val r = if(comment != null) new String(comment) else null
                          r
                        }}.filter(_!= null)

    val initValue: Array[Int] = for(tt <- testArray) yield 0
//    initValue.foreach(s => s.foreach(ss => println(s"${ss(0)} ${ss(1)} ")))
    val addToValue = (arr: Array[Int], comment: String) => {
                      var i = 0
                      for(words <- testArray){
                        for(word <- words){
                          if(comment.contains(word)) arr(i) += 1
                        }
                        i += 1
                      }
                      arr
                    }
    val mergeFun = (arr1: Array[Int], arr2: Array[Int]) => {
      val arr0 = new Array[Int](arr1.length)
      for(i <- 0 until arr0.length) arr0(i) = arr1(i) + arr2(i)
      arr0
    }

    val result: Array[Int] = rawDataRDD.aggregate(initValue)(addToValue, mergeFun)
    result.foreach(println)

    for(i <- 0 until testArray.length){
      for(w <- testArray(i))print(w + ";")
      println(result(i))
    }


  }

}
