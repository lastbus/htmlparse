package com.shgc.excel

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.{Workbook, WorkbookFactory}

import scala.StringBuilder
import scala.collection.mutable

/**
 * Created by Administrator on 2015/12/9.
 */
class ReadExcel {

  def read(path: String, names: Array[(String, Int)]): Array[Map[String, Array[String]]] ={
    val array = new Array[Map[String, Array[String]]](names.length)
    val inputStream = new FileInputStream(path)
    val wb = WorkbookFactory.create(inputStream)

    for(i <- 0 until names.length){
      array(i) = run(wb, names(i)._1, names(i)._2)
    }
    array
  }



  def run(wb: Workbook, sheetName: String, headColumn: Int): Map[String, Array[String]] ={


    val sheet = wb.getSheet(sheetName)
    val num = sheet.getRow(0).getLastCellNum
    //读取头部
    val heads = new Array[Array[String]](headColumn)
    for(i <- 0 until headColumn){
      val row = sheet.getRow(i)
      val rowArray = new Array[String](num)
      val rows1 = row.cellIterator()
      while (rows1.hasNext){
        val row = rows1.next()
        rowArray(row.getColumnIndex) = row.getStringCellValue
      }
      heads(i) = rowArray
    }

    //分别读取 剩下每列的数据，每列数据组成一个string，用 ; 分隔
    val rowN = new Array[String](num)
    for(i <- 0 until num){
      val rows = sheet.rowIterator()
      //跳过三行
      for(j <- 0 until headColumn) rows.next()
      val bf = new StringBuilder
      //读取第 i 列数据
      while (rows.hasNext){
        val row = rows.next()
        val cell = row.getCell(i)
        if(cell != null && cell.getStringCellValue.length > 0){
          bf.append(cell.getStringCellValue).append(";")
        }
      }
      if(bf.size > 0)rowN(i) = bf.toString()
    }

    //将一个sheet放入一个map中
    val sheetMap = mutable.HashMap.empty[String, Array[String]]
    for(i <- 0 until num){
      if(rowN(i) != null){
        val keyBuilder = new StringBuilder()
        for(j <- 0 until headColumn){
          keyBuilder.append(getString(heads(j), i))
          if(j != headColumn - 1) keyBuilder.append(":")
        }
        val key = keyBuilder.toString()
        if(rowN(i) != null) sheetMap(key) = rowN(i).split("[;；]").filter(d => d.length > 0)
      }
    }

    if(sheetMap.size > 0) sheetMap.toMap else null
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
