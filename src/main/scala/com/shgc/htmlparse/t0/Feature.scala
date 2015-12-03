package com.shgc.htmlparse.t0

import org.apache.spark.rdd.RDD

/**
 * Created by Administrator on 2015/12/3.
 */
trait Feature {

  def feature[k,v](rdd: RDD[(k,v)]): RDD[(k, Vector)]

}
