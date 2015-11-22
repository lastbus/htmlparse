//package com.shgc.htmlparse
//
//
//import com.shgc.htmlParser.MyHtmlParser
//import org.apache.hadoop.hbase.HBaseConfiguration
//import org.apache.hadoop.hbase.client.Put
//import org.apache.hadoop.hbase.io.ImmutableBytesWritable
//import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
//import org.apache.hadoop.io.Text
//import org.apache.hadoop.mapreduce.Job
//import org.apache.nutch.protocol.Content
//
//import org.apache.spark.SparkConf
//import org.apache.spark.SparkContext
//import org.apache.spark.rdd.{RDD, PairRDDFunctions}
//
//object SequenceFileParser{
//
//  def main(args: Array[String]){
//    if (args.length < 2) {
//        println("please input the file path and a hbase table name seperated by space");
//        System.exit(1);
//    }
//
//    val hbaseConf = HBaseConfiguration.create()
//    val hbaseSiteConfUrl = getClass().getResource("/hbase-site.xml")
//    hbaseConf.set("hbase.mapred.outputtable", args(1))
//    hbaseConf.addResource(hbaseSiteConfUrl)
//
//    val job = new Job(hbaseConf)
//    job.setOutputFormatClass(classOf[TableOutputFormat[(ImmutableBytesWritable, Put)]])
//    val hadoopConf = job.getConfiguration()
//
//    val sparkConf = new SparkConf().setAppName("Parse html file")
//    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
//
//    val sc = new SparkContext(sparkConf)
//    val rdd = sc.sequenceFile(args(0), classOf[Text], classOf[Content])
//
//    val inputRecords = sc.accumulator(0)
//    val filteredRecords = sc.accumulator(0)
//
//    val putListRdd = rdd.map( doc=>MyHtmlParser.parse(new String(doc._2.getContent), doc._2.getUrl)).filter(_!=null)
//    val kvArrayRdd = putListRdd.map(fromJavaListToScalaArray(_))
//
//    val kvRdd: RDD[(ImmutableBytesWritable, Put)] = kvArrayRdd.flatMap(x => x)
//
//    val pairRddFunction = new PairRDDFunctions(kvRdd)
//    pairRddFunction.saveAsNewAPIHadoopDataset(hadoopConf);
//    println("the records number read from sequence file:\t" + inputRecords.value.toString())
//    println("the filtered records number:\t" + filteredRecords.value)
//
//    sc.stop();
//  }
//
//  def fromJavaListToScalaArray(putList: java.util.List[Put]): Array[(ImmutableBytesWritable, Put)] = {
//    val len = if (putList != null) putList.size() else 0
//    if (len > 0) {
//      val putArray = putList.toArray(new Array[Put](len))
//        .map(put => (new ImmutableBytesWritable(put.getAttribute("key")), put))
//      putArray
//    } else {
//      null
//    }
//  }
//
//
//}
