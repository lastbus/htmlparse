package com.shgc.forumName

import java.io.{FileWriter, FileOutputStream}
import org.dom4j.{Document, Element, DocumentHelper}
import org.dom4j.io.{XMLWriter, SAXReader}
import org.jsoup.Jsoup



/**
 * Created by make on 2015/12/15.
 */
object ForumNameGenerate {
  def main(args: Array[String]): Unit ={
    val xml = DocumentHelper.parseText(initXML())

    val pcAutoArray = (new CarNamePCAuto).get("http://www.pcauto.com.cn/forum/sitemap/pp/")
    for(car <- pcAutoArray){
      insertVehicle(xml, car._1, car._2)
    }

    val xCarArray = (new CarNameXCar).get("http://www.xcar.com.cn/bbs/")
    for(car <- xCarArray){
      insertVehicle(xml, car._1, car._2)
    }

    val qqCarArray = (new CarNameTencent).get("http://club.auto.qq.com/forum.php?gid=3")
    for(car <- qqCarArray) insertVehicle(xml, car._1, car._2)


    val writer = new XMLWriter(new FileWriter("output.xml"))
    writer.write(xml)
    writer.close()

  }


  def initXML(): String = {
    // http://club.autohome.com.cn
    val url = "http://club.autohome.com.cn"
    val doc = Jsoup.connect(url).get()
    val cars = doc.select("#tab-4 .forum-tab-box .forum-brand-box p").iterator()
    val automobile = doc.select("#tab-4 .forum-tab-box .forum-brand-box ul").iterator()
    var count = 0

    val sb = new StringBuilder
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    sb.append("\n")
    sb.append("<vehicles>")
    sb.append("\n")

    while (cars.hasNext){
      val a = cars.next()
      val list = automobile.next().select("a").iterator()
      sb.append("\t<vehicle name=\"")
      sb.append(a.text())
      sb.append("\"> ")
      sb.append("\n")
      while (list.hasNext){
        val temp = list.next().text()
        sb.append("\t\t<name>")
        sb.append(temp.replace("论坛", ""))
        sb.append("</name>")
        sb.append("\n")
        count += 1
      }
      sb.append("\t</vehicle>")
      sb.append("\n")
    }
    sb.append("</vehicles>")
    println(s"autohome: ${count}")
    sb.toString()
  }

  def insertVehicle(xml: Document, name: String, cars: Array[String]): Unit ={
    val elements = xml.getRootElement.elements("vehicle").iterator()
    var has = false
    while(elements.hasNext && !has) {
      val element = elements.next().asInstanceOf[Element]
      val vehicle = element.attribute("name")
      if (vehicle.getValue.equals(name)) {
        has = true
        val elements2 = element.elements("name")
        val array = for(element <- javaList2ScalaList(elements2)) yield element.asInstanceOf[Element].getText
        for(car <- cars){
          if(!array.contains(car)){
            element.addElement("name").addText(car)
          }
        }
      }
    }

    if(!has){
      val element = xml.getRootElement.addElement("vehicle").addAttribute("name", name)
      for(car <- cars) element.addElement("name").setText(car)
    }

  }

  implicit def javaList2ScalaList(list: java.util.List[_]): Array[_] = {
    if (list.size() == 0) return null
    val array = new Array[Any](list.size())
    for(i <- 0 until list.size()){
      array(i) = list.get(i)
    }
    array
  }

}
