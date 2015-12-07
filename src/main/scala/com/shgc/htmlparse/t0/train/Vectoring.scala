//package com.shgc.htmlparse.t0.train
//
//import com.shgc.htmlparse.t0.Feature
//import org.apache.spark.mllib.linalg.Vectors
//import org.apache.spark.rdd.RDD
//
///**
// * Created by Administrator on 2015/12/3.
// */
//class Vectoring extends Feature{
//
//  override def feature[k, v](rdd: RDD[(k, v)]): RDD[(k, org.apache.spark.mllib.linalg.Vector)] = {
//    rdd.map(d => (d._1, Vectors.dense(1,2)))
//  }
//
//}
