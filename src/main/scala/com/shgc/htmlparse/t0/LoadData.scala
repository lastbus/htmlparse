package com.shgc.htmlparse.t0

import org.apache.spark.rdd.RDD

/**
 * Created by Administrator on 2015/12/3.
 */
trait LoadData {

  def getData[k,v](): RDD[(k, v)]

}
