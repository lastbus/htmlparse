package com.shgc.analysis

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Put, Result, Scan}
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Bytes, Base64}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
 * Created by make on 2015/12/10.
 */
object HBaseSparkUtil extends Serializable{
  val hBaseConf = HBaseConfiguration.create()

  private def convertScanToString(scan: Scan): String ={
    val proto = ProtobufUtil.toScan(scan)
    Base64.encodeBytes(proto.toByteArray)
  }

  def getAllRDD(sc: SparkContext, tableName: String, columnFamily: String, column: String): RDD[(String, String)] ={
    val columnFamilyBytes = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable], classOf[Result]).
      map(d =>{
        val key = d._2.getRow
        val value = d._2.getValue(columnFamilyBytes, columnBytes)
        if(key != null && value != null)(new String(key), new String(value)) else null
      })
  }

  /**
   * 根据网站取数据
   * @param sc
   * @param website
   * @param tableName
   * @return
   */
  def getWebsiteRDD(sc: SparkContext, website: String, tableName: String, columnFamily: String, column: String): RDD[(String, String)] ={
    val scan = new Scan()
    val columnFamilyBytes = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    scan.addColumn(columnFamilyBytes, columnBytes)
    scan.setStartRow(Bytes.toBytes(website))
    scan.setStopRow(Bytes.toBytes(website + "}"))
//    scan.setRowPrefixFilter(Bytes.toBytes(website))
//    scan.setFilter(new PrefixFilter(Bytes.toBytes(website)))
    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan))
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)

    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat],
                        classOf[ImmutableBytesWritable], classOf[Result]).
      map(d =>{
        val key = d._2.getRow
        val value = d._2.getValue(columnFamilyBytes, columnBytes)
        if(key != null && value != null)(new String(key), new String(value)) else null
      })
  }

  /**
   *
   * @param sc
   * @param vehicleBand
   * @param tableName
   * @param columnFamily
   * @param column
   * @return
   */
  def getVehicleBandRDD(sc: SparkContext, vehicleBand: String, tableName: String, columnFamily: String, column: String): RDD[(String, String)] ={
    if(sc == null) return null
    val vehicle = "^.*\\|" + vehicleBand + "\\|.+\\|2015\\d{10}\\|.*"
    val scan = new Scan()
    val columnFamilyBytes  = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    scan.addColumn(columnFamilyBytes, columnBytes)
    val comp = new RegexStringComparator(vehicle)
    val filter = new RowFilter(CompareOp.EQUAL, comp)
    scan.setFilter(filter)
    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan))
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable], classOf[Result]).
      map(d =>{
        val key = d._2.getRow
        val value = d._2.getValue(columnFamilyBytes, columnBytes)
        if(key != null && value != null)(new String(key), new String(value)) else null
      })
  }

  /**
   * 取出某一车型的所有数据
   * @param sc
   * @param carType
   * @param tableName
   * @return
   */
  def getCarTypeRDD(sc: SparkContext,vehicleBand: String, carType: String,
                    tableName: String, columnFamily: String, column: String): RDD[(String, String)] ={
    val carName = "\\w{2,8}\\|" + vehicleBand + "\\|" + carType + "\\|2015\\d{10}\\|.+"
    val scan = new Scan()
    val columnFamilyBytes  = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    scan.addColumn(columnFamilyBytes, columnBytes)
    val comp = new RegexStringComparator(carName)
    val filter = new RowFilter(CompareOp.EQUAL, comp)
    scan.setFilter(filter)
    sc.hadoopConfiguration.addResource("hbase-site.xml")
//    sc.hadoopConfiguration.set("hbase.rpc.timeout", "120000")
    sc.hadoopConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan))
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat],
          classOf[ImmutableBytesWritable], classOf[Result]).
      map(d =>{
        val key = d._2.getRow
        val value = d._2.getValue(columnFamilyBytes, columnBytes)
        if(key != null && value != null)(new String(key), new String(value)) else null
      })
  }

  /**
   * 取出某段时间的所有车型
   * @param sc
   * @param timeIntervalRegex
   * @param tableName
   * @return
   */
  def getTimeInterval(sc: SparkContext, timeIntervalRegex: String,
                      tableName: String, columnFamily: String, column: String): RDD[(String, String)] ={
    val scan = new Scan()
    val columnFamilyBytes  = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    scan.addColumn(columnFamilyBytes, columnBytes)
    val comp = new RegexStringComparator(timeIntervalRegex)
    val filter = new RowFilter(CompareOp.EQUAL, comp)
    scan.setFilter(filter)

    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan))
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)

    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat],
                classOf[ImmutableBytesWritable], classOf[Result]).
    map(d =>{
      val key = d._2.getRow
      val value = d._2.getValue(columnFamilyBytes, columnBytes)
      if(key != null && value != null)(new String(key), new String(value)) else null
    })
  }

  /**
   * 综合搜索
   * @param website
   * @param carType
   * @param timeIntervalRegex
   * @return
   */
  def select(sc: SparkContext, tableName: String, columnFamily: String, column: String,
                    website: String = null, carType: String = null, timeIntervalRegex: String = null): RDD[(String, String)] ={
    val scan = new Scan()
    val columnFamilyBytes  = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    scan.addColumn(columnFamilyBytes, columnBytes)

    val filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL)
    if(website!=null)filterList.addFilter(new PrefixFilter(Bytes.toBytes(website)))
    if(carType != null) filterList.addFilter(new RowFilter(CompareOp.EQUAL, new SubstringComparator(carType)))
    if(timeIntervalRegex != null) filterList.addFilter(new RowFilter(CompareOp.EQUAL, new RegexStringComparator(timeIntervalRegex)))
    if(filterList.getFilters.size() != 0) scan.setFilter(filterList)
    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
    sc.hadoopConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan))
    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result]).
      map(d =>{
        val key = d._2.getRow
        val value = d._2.getValue(columnFamilyBytes, columnBytes)
        if(key != null && value != null)(new String(key), new String(value)) else null
      })
  }

  /**
   * 取出某一列值
   * @param sc
   * @param tableName
   * @param columnFamily
   * @param column
   * @return
   */
  def getCellValueRDD(sc: SparkContext, tableName: String, columnFamily: String, column: String): RDD[(String, String)] ={
    val scan = new Scan()
    val columnFamilyBytes  = Bytes.toBytes(columnFamily)
    val columnBytes = Bytes.toBytes(column)
    scan.addColumn(columnFamilyBytes, columnBytes)
    sc.hadoopConfiguration.addResource("hbase-site.xml")
    sc.hadoopConfiguration.set(TableInputFormat.INPUT_TABLE, tableName: String)
    sc.hadoopConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan))
    sc.newAPIHadoopRDD(sc.hadoopConfiguration, classOf[TableInputFormat],
    classOf[ImmutableBytesWritable], classOf[Result]).map(d =>{
      val key = d._2.getRow
      val value = d._2.getValue(columnFamilyBytes, columnBytes)
      if(key != null && value != null)(new String(key), new String(value)) else null
    })
  }

}
