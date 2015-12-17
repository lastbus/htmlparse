package com.shgc.forumName

import org.dom4j.io.SAXReader
import org.dom4j.Element

import scala.collection.mutable

/**
 * Created by Administrator on 2015/12/17.
 */
class LoadCarBandName(path: String) {
  val typeBandMap = new mutable.HashMap[String, String]
  val xml = (new SAXReader()).read(path)
  val elements = xml.getRootElement.elementIterator("vehicle")
  var count = 0
  while (elements.hasNext){
    val element = elements.next().asInstanceOf[Element]
    val carBand = element.attributeValue("name")
    println("========" + carBand + "============")
    val nodes = element.elementIterator("name")
    while (nodes.hasNext){
      val carName = nodes.next().asInstanceOf[Element].getText
      typeBandMap.put(carName, carBand)
      count += 1
      if(count != typeBandMap.size) {
        println(carName)
        count -= 1
      }
    }
  }
  println(count)

}

object LoadCarBandName{
  def main(args: Array[String]): Unit ={
//    val test = new LoadCarBandName("output.xml")
//    println(test.typeBandMap.size)
    val test2 = loadAutoHome()
//    for((key, value) <- test2){
//      println(key + "  " + value)
//    }

  }

  def loadAutoHome(): Map[String, String] ={
    val cars = (new CarNameAutoHome).get("http://club.autohome.com.cn")
    val bandAndCarType = new mutable.HashMap[String, String]
    var count = 0
    for(car <- cars){
      for(c <- car._2){
        if(bandAndCarType.getOrElse(c, null) != null){
          println(s"conflict: ${c}   " + bandAndCarType(c))
          println(s"${c} : ${car._1}")
        }else{
          bandAndCarType(c) = car._1
        }

      }
    }
    if(count != bandAndCarType.size){
      println("There are conflict!")
    }
    bandAndCarType.toMap
  }
}
