package com.shgc.htmlparse.util

import java.util

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client._

/**
 * Created by make on 2015/12/2.
 */
class HBaseUtil {

  val config = HBaseConfiguration.create()
  config.addResource("hbase-site.xml")
  // Connections are heavyweight, create one once and keep it around.
  val connection = ConnectionFactory.createConnection(config)


//  val filter = new SingleColumnValueFilter(
//    cf,
//    column,
//    CompareOp.EQUAL,
//    Bytes.toBytes("my value")
//  )
  def table(tableName: String): Table ={
    // before make sure the table exits
    connection.getTable(TableName.valueOf(tableName))
  }

  def put(table: Table, put: Put): Int = {
    try{ table.put(put) } catch {
      case _ : Exception => return -1}
    0
  }

  def put(table: Table, puts: java.util.List[Put]): Int = {
    try{ table.put(puts) } catch {
      case _ : Exception => return -1}
    0
  }

  def put(table: Table, puts: Array[Put]): Int = {
    try{ table.put(puts) } catch {
      case _ : Exception => return -1}
    0
  }

  def get(table: Table, get: Get): Unit ={
    val result = table.get(get)
  }

  def scan(): Unit ={
    val scan = new Scan()
    scan.addColumn(Bytes.toBytes("comments"), Bytes.toBytes("username"))
    val scanner = table("hh").getScanner(scan)
    var rr: Result = null
    while({rr = scanner.next(); rr!= null}){
      println("Found row: " + rr)


    }

  }





  implicit def array2JavaList(array: Array[Put]): java.util.List[Put]={
    val list = new util.ArrayList[Put]
    for (put <- array) list.add(put)
    list
  }

}
