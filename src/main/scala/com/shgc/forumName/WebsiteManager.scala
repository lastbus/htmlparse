package com.shgc.forumName

import org.dom4j.{Element, Document}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Administrator on 2015/12/17.
 */
class WebsiteManager(xml: Document) {
  private val carBandNameArray = new ArrayBuffer[Array[(String, Array[String])]]

  def addWebsite(carBandName: Array[(String, Array[String])]): WebsiteManager ={
    carBandNameArray += carBandName
    this
  }

  def execute(): Unit ={
    if(carBandNameArray.length < 1) return
    for(carBandName <- carBandNameArray){
      for(car <- carBandName){
        insertVehicle(xml, car._1, car._2)
      }
    }
//    countCarNames(xml)
    println("=================")
  }

  //记录原来有几个品牌，几个车型，增加几个品牌、车型
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

  def countCarNames(xml: Document): Unit = {
    val root = xml.getRootElement.elements("vehicle").iterator()
    var carBand = 0
    var carName = 0
    while (root.hasNext){
      carBand += 1
      val elements = root.next().asInstanceOf[Element].elements("name").iterator()
      while (elements.hasNext) carName += 1
    }
    println(s"carBand: ${carBand}, carName: ${carName}")
  }

  def javaList2ScalaList(list: java.util.List[_]): Array[_] = {
    if (list.size() == 0) return null
    val array = new Array[Any](list.size())
    for(i <- 0 until list.size()){
      array(i) = list.get(i)
    }
    array
  }

}
