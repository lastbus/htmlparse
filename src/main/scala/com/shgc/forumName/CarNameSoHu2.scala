package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/12/17.
 */
class CarNameSoHu2 extends GetCarName{

  override def get(url: String): Array[(String, Array[String])] = {
    val document = Jsoup.connect(url).get()
    val lists = document.select("div.tw_bbs_daquan_rbox div.cars_daquan_boxb dl:gt(0)")
    val carBandAndNameArray = new Array[(String, Array[String])](lists.size())
    var carBand, carName = 0
    for(i <- 0 until lists.size()){
      val element = lists.get(i)
      val  carType = element.select("dt").text().trim
      val cars = element.select("dd ul li a")
      val carsNameArray = new Array[String](cars.size())
      for(j <- 0 until cars.size()){
        carsNameArray(j) = cars.get(j).text().replace("车友会", "").trim
        carName += 1
      }
      carBand += 1
      carBandAndNameArray(i) = (carType, carsNameArray)
    }

    println(s"sohu car band: ${carBand}")
    println(s"sohu car name: ${carName}")
    carBandAndNameArray.filter(arr => arr._2 != null && arr._2.length > 0)
  }
}

object CarNameSoHu2 {

  def main(args: Array[String]): Unit ={
    val test = (new CarNameSoHu2).get("http://saa.auto.sohu.com/search/clublist.shtml")

    for(tt <- test){
      println(s"car band: ${tt._1}")
      for(t <- tt._2){
        println(t)
      }
    }
  }


}

