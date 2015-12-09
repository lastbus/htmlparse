package com.shgc.excelTest

import java.io.{FileOutputStream, FileInputStream}

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Test

/**
 * Created by Administrator on 2015/12/9.
 */
@Test
class ReadExcelTest {

  @Test
  def read = {
    val inputStream = new FileInputStream("E:2015年长安乘用车BTS-乘用车词库-1120.xlsx")
    val wb = WorkbookFactory.create(inputStream)
    val wb2 = new XSSFWorkbook

    val sheets = wb.sheetIterator()
    while (sheets.hasNext){
      val sheet = sheets.next()
      wb2.createSheet(sheet.getSheetName)
      val rows = sheet.rowIterator()
      while (rows.hasNext){
        val row = rows.next()
        val row2 = wb2.getSheet(sheet.getSheetName).createRow(row.getRowNum)
        val cells = row.cellIterator()
        while (cells.hasNext){
          val cell = cells.next()
          val cell2 = row2.createCell(cell.getColumnIndex)
          val cellValue = cell.getStringCellValue
          if(cellValue != null && cellValue.length > 0) {
            cell2.setCellValue("test")
            if(cell2.getStringCellValue.length < 1) println("error")
          }
          cell.setCellValue("test2")
        }
      }
      println(sheet.getSheetName)
    }

    val fileOutputStream = new FileOutputStream("E:2015年长安乘用车BTS-乘用车词库-1120 副本.xlsx")
    wb.write(fileOutputStream)
    fileOutputStream.close()
    inputStream.close()
  }
}
