package com.shgc.htmlparse.t0

import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vector

/**
 * Created by Administrator on 2015/12/3.
 */
trait Forecast {

  def run[k, v1](rdd: RDD[(k, Vector)]): RDD[(k, Double)]
}
