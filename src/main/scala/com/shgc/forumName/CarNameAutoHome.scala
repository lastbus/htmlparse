package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/12/17.
 */
class CarNameAutoHome extends GetCarName{
  override def get(url: String): Array[(String, Array[String])] = {
    val doc = Jsoup.connect(url).get()
    val cars = doc.select("#tab-4 .forum-tab-box .forum-brand-box p")
    val automobile = doc.select("#tab-4 .forum-tab-box .forum-brand-box ul")
    val carTypeAndNameArray = new Array[(String, Array[String])](cars.size)
    var bandNum, typeCount = 0
    for(i <- 0 until cars.size()){
      val carType = cars.get(i).text().trim
      val elements = automobile.get(i).select("a")
      val carNameArray = new Array[String](elements.size())
      for(j <- 0 until elements.size){
        carNameArray(j) = elements.get(j).text().replace("论坛", "").trim
        typeCount += 1
      }
      bandNum += 1
      carTypeAndNameArray(i) = (carType, carNameArray)
    }
    carTypeAndNameArray
  }
}

object CarNameAutoHome {

  def main (args: Array[String]){
    val test = (new CarNameAutoHome).get("http://club.autohome.com.cn")
    for(tt <- test){
      println(tt._1)
      for(t <- tt._2){
        println(t)
      }
    }
  }
}
