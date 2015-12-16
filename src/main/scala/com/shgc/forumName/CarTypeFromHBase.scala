package com.shgc.forumName

import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat

/**
 * Created by Administrator on 2015/12/15.
 */
object CarTypeFromHBase {

  def main(args: Array[String]): Unit ={
    val sc = SparkManagerFactor.getSparkContext(CarTypeFromHBase.getClass.getName)
    val hBaseConf = SparkManagerFactor.getHBaseConf("hh",TableInputFormat.INPUT_TABLE)

    val data = sc.newAPIHadoopRDD(hBaseConf, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable], classOf[Result]).map(d => {
      val key = new String(d._2.getRow).split("\\|")
      (key(0).trim, key(1).replace("#", "").trim)
    }).distinct().coalesce(1).saveAsTextFile("/user/hdfs/temp2/car-type")


  }

}
