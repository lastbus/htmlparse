package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/12/17.
 */
class CarNameSina extends GetCarName{

  override def get(url: String): Array[(String, Array[String])] = {
    val document = Jsoup.connect(url).get()
    val lists = document.select("#category_5 tbody tr th")
    val carBandAndNameArray = new Array[(String, Array[String])](lists.size())
    var carBand, carName = 0
    for(i <- 0 until lists.size()){
      val element = lists.get(i)
      val  carType = element.select("h2 a").text().trim
      val carType2 = if(carType.endsWith("汽车")) carType.substring(0, carType.indexOf("汽车")) else carType
      val cars = element.select("p a[href^=/]")
      val carsNameArray = new Array[String](cars.size())
      for(j <- 0 until cars.size()){
        carsNameArray(j) = cars.get(j).text().trim
        carName += 1
      }
      carBand += 1
      carBandAndNameArray(i) = (carType2, carsNameArray)
    }

    println(s"sina car band: ${carBand}")
    println(s"sina car name: ${carName}")
    carBandAndNameArray.filter(arr => arr._2 != null && arr._2.length > 0)
  }
}


object CarNameSohu {

  def main(args: Array[String]): Unit = {
    val test = (new CarNameSina).get("http://bbs.auto.sina.com.cn/")

    for (tt <- test) {
      println(s"car band: ${tt._1}")
      for (t <- tt._2) {
        println(t)
      }
    }
  }
}
