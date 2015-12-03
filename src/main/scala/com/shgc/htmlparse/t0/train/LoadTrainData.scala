package com.shgc.htmlparse.t0.train

import com.shgc.htmlparse.t0.LoadData
import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.spark.rdd.RDD

/**
 * Created by Administrator on 2015/12/3.
 */
class LoadTrainData extends LoadData {

  override def getData[k, v](): RDD[(String, Int)] = {
    val sc = SparkManagerFactor.getSparkContext("oo")
    sc.textFile("/user/hdfs/temp/urls/urls1.txt").
      map(line => (line.trim, 1))

  }
}
