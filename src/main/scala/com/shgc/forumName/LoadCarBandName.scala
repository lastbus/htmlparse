package com.shgc.forumName

import org.dom4j.io.SAXReader
import org.dom4j.Element

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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

    loadAutoHome
    loadBitAuto
    loadPcAuto
    loadSina
    loadSoHu
    loadTencent
    loadXCar
  }

  def loadAutoHome: Map[String, String] ={
    println("======== auto home ========")
    val cars = (new CarNameAutoHome).get("http://club.autohome.com.cn")
    val bandAndCarType = array2Map(cars)
    bandAndCarType
  }

  def loadBitAuto: Map[String, String] = {
    println("======== bit auto ========")
    val cars = (new CarNameBitAuto).get("http://baa.bitauto.com/foruminterrelated/brandforumlist_by_pinpai.html")
    val bandAndCarType = array2Map(cars)
    bandAndCarType
  }

  def loadPcAuto: Map[String, String] = {
    println("======== pcautohome ========")
    val cars = (new CarNamePCAuto).get("http://www.pcauto.com.cn/forum/sitemap/pp/")
    val bandAndCarType = array2Map(cars)
    bandAndCarType
  }

  def loadSina: Map[String, String] = {
    println("=======  sina  ========")
    val cars = (new CarNameSina).get("http://bbs.auto.sina.com.cn/")
    val bandAndCarType = array2Map(cars)
    bandAndCarType
  }

  def loadSoHu: Map[String, String] = {
    println("=======  sohu  ===========")
    val cars = (new CarNameSoHu2).get("http://saa.auto.sohu.com/search/clublist.shtml")
    val bandAndCarType = array2Map(cars)
    bandAndCarType

  }

  def loadTencent: Map[String, String] = {
    println("=======  tencent  =====")
    val cars = (new CarNameTencent).get("http://club.auto.qq.com/forum.php?gid=3")
    val bandAndCarType = array2Map(cars)
    bandAndCarType

  }
  def loadXCar: Map[String, String] = {
    println("=======  xcar  =====")
    val cars = (new CarNameXCar).get("http://www.xcar.com.cn/bbs/")
    val bandAndCarType = array2Map(cars)
    bandAndCarType
  }


  def array2Map(array: Array[(String, Array[String])]): Map[String, String] = {
    val bandAndCarType = new mutable.HashMap[String, String]
    val removeArray = new ArrayBuffer[String]()
    var count = 0
    for(car <- array){
      for(c <- car._2 if(c.length > 0)){
        count += 1
        if(bandAndCarType.getOrElse(c, null) != null){
          println(s"bitauto conflict: (${c} ,${bandAndCarType(c)}), (${c}, ${car._1})")
          removeArray += c
        }else{
          bandAndCarType(c) = car._1
        }
      }
    }
    println(s"cars numbers: ${count}, different number: ${bandAndCarType.size}")
    bandAndCarType.filter(keyValue => !removeArray.contains(keyValue._1)).toMap
  }
}
