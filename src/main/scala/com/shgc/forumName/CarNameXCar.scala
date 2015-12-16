package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by make on 2015/12/16.
 */
class CarNameXCar extends GetCarName{
  override def get(url: String): Array[(String, Array[String])] = {
    val document = Jsoup.connect(url).get()
    val lists = document.select("#forum_main .forum_cont .t0922con_nt table")
    val carTypeAndCarNameArray = new Array[(String, Array[String])](lists.size())
    var count = 0
    for (i <- 0 until lists.size){
      val element = lists.get(i).select("tbody tr")
      val carType = element.select("td:eq(0) a:last-child").text().trim
      val cars = element.select("td:eq(1) .t1203_fbox")
      val carNameArray = new Array[String](cars.size())
      for (j <- 0 until cars.size()){
        carNameArray(j) = cars.get(j).select("a").text().replace("论坛", "").trim
        count +=  1
      }
      carTypeAndCarNameArray(i) = (carType, carNameArray)
    }
    println(s"xcar type: ${lists.size()}")
    println(s"xcar count: ${count}")
    carTypeAndCarNameArray
  }
}

object CarNameXCar {

  def main(args: Array[String]): Unit ={
    val test = (new CarNameXCar).get("http://www.xcar.com.cn/bbs/")

    for(tt <- test){
      println(s"carType: ${tt._1}")
      for(t <- tt._2){
        print(t + "\t")
      }
      println
    }

    println(test.size)
  }
}