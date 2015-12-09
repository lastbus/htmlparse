package com.shgc.excel

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Created by Administrator on 2015/12/9.
 */
class ReadExcel {

  def read(path: String): Unit ={
    val inputStream = new FileInputStream(path)
    val workBook = WorkbookFactory.create(inputStream)

  }

}
