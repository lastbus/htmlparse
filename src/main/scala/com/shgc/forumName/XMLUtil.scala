package com.shgc.forumName

import java.io.FileWriter

import com.shgc.study.packageAndImports.bobsrockets.launch.TestVehicle
import org.dom4j.Element
import org.dom4j.io.{SAXReader, XMLWriter}

import scala.collection.mutable

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

    val read = loadVehicle("autohome.xml")

    for((key, value) <- read){
      println(key + " , " + value)
    }

    println(read.size)
  }

  def initial() = {
    val arrayBuffer = new mutable.ArrayBuffer[(String, String, String)]
    arrayBuffer += (("com.shgc.forumName.CarNamePCAuto",
      "http://www.pcauto.com.cn/forum/sitemap/pp/", "pcauto.xml"))
    arrayBuffer += (("com.shgc.forumName.CarNameAutoHome",
      "http://club.autohome.com.cn", "autohome.xml"))
    arrayBuffer += (("com.shgc.forumName.CarNameBitAuto",
      "http://baa.bitauto.com/foruminterrelated/brandforumlist_by_pinpai.html", "bitauto.xml"))
    arrayBuffer += (("com.shgc.forumName.CarNameSina",
      "http://bbs.auto.sina.com.cn/", "sina.xml"))
    arrayBuffer += (("com.shgc.forumName.CarNameSoHu2",
      "http://saa.auto.sohu.com/search/clublist.shtml",
      "sohu.xml"))
    arrayBuffer += (("com.shgc.forumName.CarNameTencent",
      "http://club.auto.qq.com/forum.php?gid=3", "tencent.xml"))
    arrayBuffer += (("com.shgc.forumName.CarNameXCar",
      "http://www.xcar.com.cn/bbs/", "xcar.xml"))

    execute(arrayBuffer.toArray)
  }

  def execute(classUrlPath: Array[(String, String, String)]): Unit = {
    var success = false
    var count = 0
    for((className, url, savePath) <- classUrlPath){
      count = 3
      success = false
      while (!success && count > 0){
        try{
          val t = Class.forName(className).newInstance().asInstanceOf[GetCarName]
          val tt =t.get(url)
          saveToPath(tt, savePath)
          success = true
        }catch {
          case _: Exception => {println("error: url"); Thread.sleep(2000)}
        }
      }
    }

  }


  def saveToPath(test: Array[(String, Array[String])], path: String): Unit = {
    val xml = generateXML(test)
    val writer = new FileWriter(path)
    writer.write(xml)
    writer.close()
  }

  /**
   * 读取汽车品牌配置文件
   * @param path
   * @return
   */
  def loadVehicle(path: String): Map[String, String] = {
    var count = 0
    val xml = (new SAXReader()).read(path)
    val elements = xml.getRootElement.elementIterator("vehicle")
    val vehicleMap = new mutable.HashMap[String, String]()
    while (elements.hasNext){
      val element = elements.next().asInstanceOf[Element]
      val band = element.attribute("name").getText.trim
      val nodes = element.elementIterator("name")
      while (nodes.hasNext){
        count += 1
        val car = nodes.next().asInstanceOf[Element]
        val name = car.getText.trim
        if(vehicleMap.getOrElse(name, null) == null){
          vehicleMap(name) = band
        }else{
          println(s"conflict: ${path} : ${name}")
          println("delete: " + (name, band) + " , " + (name, vehicleMap(name)))
          vehicleMap.remove(name)
        }
      }
    }
    vehicleMap.toMap
  }


}

