package com.shgc.forumName

import java.io.FileWriter

import com.shgc.study.packageAndImports.bobsrockets.launch.TestVehicle
import org.dom4j.io.XMLWriter

/**
 * Created by Administrator on 2015/12/21.
 */
object XMLUtil {

  def generateXML(vehicle: Array[(String, Array[String])]): String = {
    val sb = new StringBuilder
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    sb.append("\n")
    sb.append("<vehicles>")

    insertVehicle(sb, vehicle)

    sb.append("\n")
    sb.append("</vehicles>")
    sb.toString()
  }

  def insertVehicle(sb: StringBuilder, vehicles: Array[(String, Array[String])]): Unit = {
    for(vehicle <- vehicles){
      sb.append("\t<vehicle name=\"")
      sb.append(vehicle._1)
      sb.append("\"> ")
      sb.append("\n")
      for(carName <- vehicle._2){
        sb.append("\t\t<name>")
        sb.append(carName)
        sb.append("</name>")
        sb.append("\n")
      }
      sb.append("\t</vehicle>")
      sb.append("\n")
    }

  }

  def main(args: Array[String]): Unit ={
    var success = false
    while (success){
      val test1 = (new CarNamePCAuto).get("http://www.pcauto.com.cn/forum/sitemap/pp/")
      saveToPath(test1, "pcauto.xml")
      success = true
    }
    
    success = false
    while (success){
      val test2 = (new CarNameAutoHome).get("http://club.autohome.com.cn")
      saveToPath(test2, "autohome.xml")
      success = true
    }

    success = false
    while (success){
      val test3 = (new CarNameBitAuto).get("http://baa.bitauto.com/foruminterrelated/brandforumlist_by_pinpai.html")
      saveToPath(test3, "bitauto.xml")
      success = true
    }

    success = false
    while (success){
      val test4 = (new CarNameSina).get("http://bbs.auto.sina.com.cn/")
      saveToPath(test4, "sina.xml")
      success = false
    }


    success = false
    while (success){
      val test5 = (new CarNameSoHu2).get("http://saa.auto.sohu.com/search/clublist.shtml")
      saveToPath(test5, "sohu.xml")
      success = true
    }

    success = false
    while (!false){
      val test6 = (new CarNameTencent).get("http://club.auto.qq.com/forum.php?gid=3")
      saveToPath(test6, "tencent.xml")
      success = false
    }
    success = false
    while (!success){
      val test7 = (new CarNameXCar).get("http://www.xcar.com.cn/bbs/")
      saveToPath(test7, "xcar.xml")
      success = true
    }

  }

  def saveToPath(test: Array[(String, Array[String])], path: String): Unit = {
    val xml = generateXML(test)
    val writer = new FileWriter(path)
    writer.write(xml)
    writer.close()
  }
}

