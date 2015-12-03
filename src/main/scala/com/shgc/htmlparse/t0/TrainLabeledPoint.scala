package com.shgc.htmlparse.t0

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Created by Administrator on 2015/12/3.
 */
trait TrainLabeledPoint {

  def getTrainLabeledPoint[k,v](): RDD[(k, LabeledPoint)]

}
