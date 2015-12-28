package com.shgc.sparkSQL

import com.shgc.htmlparse.util.SparkManagerFactor
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.StructType

/**
 * Created by Administrator on 2015/12/25.
 */
object Test {

  /**
   * inferring the schema using case class
   * @param args
   */
  def main(args: Array[String]): Unit ={
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    case class MiddleResult(key: String, website: String, userName: String,
                            firstLv: String, secondLv: String, thirdLv: String,
                            remove: String, emotion: String)

    val dataFrame = sc.textFile("/user/hdfs/temp2/analyze").map(d => d.split(",")).
      map(d => MiddleResult(d(0), d(1), d(2), d(3), d(4), d(5), d(6), d(7))).toDF

    dataFrame.registerTempTable("t")
    val r1 = sqlContext.sql("select website from t")
    r1.take(3).foreach(println)
    sqlContext
    println(r1.count)

  }

  def dynamicsSchema[T](rdd: RDD[Row],schemaString: String): Unit ={
    val sc = SparkManagerFactor.getSparkContext(this.getClass.getName)
    val sqlContext = new SQLContext(sc)

    import org.apache.spark.sql.types.{StringType, StructField, StringType}

    val schema = StructType(schemaString.split(" ").
      map(fieldName => StructField(fieldName, StringType, true)))
    val dataFrame = sqlContext.createDataFrame(rdd, schema)

    val dd = new org.apache.spark.sql.hive.HiveContext(sc)

  }


}
