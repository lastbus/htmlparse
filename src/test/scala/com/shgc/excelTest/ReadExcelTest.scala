package com.shgc.excelTest

import java.io.{FileOutputStream, FileInputStream}

import com.shgc.excel.ReadExcel
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Test

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable

/**
 * Created by Administrator on 2015/12/9.
 */
@Test
class ReadExcelTest {

  @Test
  def readTest = {
    val readExcel = new ReadExcel
    val names = Array(("产品关注点", 3), ("车主对产品的满意点", 3), ("车主对产品的抱怨点", 3), ("服务关注点", 2), ("品牌关注点", 3))
    val sheets = readExcel.read("E:2015年长安乘用车BTS-乘用车词库-1120.xlsx", names)
    if(sheets != null){
      for(sheet <- sheets){
        println(sheet.size)
        for((key, value) <- sheet){
          print(key + " |||| ")
          value.foreach(d =>  print(d + d.length+ "   "))
          println("\n++++++++++++++++++")
        }
        Console println "-=-=-=-=-=-=-=-=-=-="
      }

    }
}


//  @Test
  def test1 = {
    val inputStream = new FileInputStream("E:2015年长安乘用车BTS-乘用车词库-1120.xlsx")
    val wb = WorkbookFactory.create(inputStream)
    val sheets = wb.getSheetAt(1)
    //产品：动力性：动力充足性：关键词
    val row0 = sheets.getRow(0)
    val head = row0.getCell(0).getStringCellValue

    println(head)
    println("========")
    val row1 = sheets.getRow(1)
    val length = row1.getLastCellNum
    val row1Array = new Array[String](length)
    val cells = row1.cellIterator()
    while (cells.hasNext){
      val cell = cells.next()
      row1Array(cell.getColumnIndex) = cell.getStringCellValue
    }
    row1Array.filter(_.length > 1).foreach(println)
    println("========")

    val row2 = sheets.getRow(2)
    val row2Array = new Array[String](length)
    val cells2 = row2.cellIterator()
    while (cells2.hasNext){
      val cell = cells2.next()
      row2Array(cell.getColumnIndex) = cell.getStringCellValue
    }
    row2Array.filter(_.length > 1).foreach(println)
    println("========")

    val rowN = new Array[String](length)
    for(i <- 0 until length){
      val rows = sheets.rowIterator()
      rows.next()
      rows.next()
      rows.next()
      val stringBuffer = new StringBuffer()
      while (rows.hasNext){
        val row = rows.next
        val cellValue = row.getCell(i)
        if(cellValue != null && cellValue.getStringCellValue.length > 0)stringBuffer.append(cellValue).append(";")
      }
      if(stringBuffer.length() > 0) rowN(i) = stringBuffer.toString
    }
    rowN.filter(_!= null).foreach(println)
    println("======")

    val map = mutable.HashMap.empty[String, Array[String]]
    for(i <- 0 until rowN.length){
      val key = head + ":" + getString(row1Array, i) + ":" + getString(row2Array, i)
      map(key) = rowN(i).split("[;；]")
    }

    for((key, value)<-map){
      print(key + "  ")
      value.foreach(v => print(v + " "))
      println()
    }




  }

  def getString(array: Array[String], index: Int): String ={
    var i = index
    while (i >= 0){
      if(array(i) != null && array(i).length > 1) return array(i)
      i -= 1
    }
    null
  }




}
