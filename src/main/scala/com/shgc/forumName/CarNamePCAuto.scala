package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/12/16.
 */
class CarNamePCAuto extends GetCarName{
  override def get(url: String): Array[(String, Array[String])] = {
    val document = Jsoup.connect(url).get()
    val lists = document.select(".wrap .tbLetters table tbody tr:gt(0)")
    val carTypeAndCarNameArray = new Array[(String, Array[String])](lists.size())
    var count = 0
    for(i <- 0 until lists.size()){
      val element = lists.get(i)
      val carType = element.select("td:eq(0) i").text()
      val cars = element.select("td:eq(1) dl dd .dTit a.hei")
      val carNameArray = new Array[String](cars.size)
      for(j <- 0 until cars.size()){
        carNameArray(j) = cars.get(j).text()
        count += 1
      }
      carTypeAndCarNameArray(i) = (carType, carNameArray)
    }
    println(s"pcauto car type: ${lists.size()}")
    println(s"pcauto: ${count}")
    carTypeAndCarNameArray
  }
}

object CarNamePCAuto {

  def main(args: Array[String]): Unit ={
    val test = new CarNamePCAuto
    val result = test.get("http://www.pcauto.com.cn/forum/sitemap/pp/")
    var i = 0
    for(car <- result){
      println(s"carType: ${car._1}")
      for(c <- car._2){
        i += 1
        print(c + "\t")
      }
      println
    }

    println(s"total number: ${i}")
  }


}