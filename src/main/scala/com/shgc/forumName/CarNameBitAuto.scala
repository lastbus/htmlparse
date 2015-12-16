package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/12/16.
 */
class CarNameBitAuto extends GetCarName{
  override def get(url: String): Array[(String, Array[String])] = {
    val document = Jsoup.connect(url).get()
    val lists = document.select("#lineboxpage > .pinpaibox dl.list dd")

    val carTypeAndCarNameArray = new Array[(String, Array[String])](lists.size())
    var count = 0
    for(i <- 0 until lists.size){
      val element = lists.get(i)
      val carType = element.select(".pplogo a").text().trim
      val carNames = element.select("ul li a")
      val carsArray = new Array[String](carNames.size)
      for(j <- 0 until carNames.size()){
        carsArray(j) = carNames.get(j).text().trim
        count += 1
      }
      carTypeAndCarNameArray(i) = (carType, carsArray)
    }
    println(s"bitauto car type: ${lists.size()}")
    println(s"car number: ${count}")
    carTypeAndCarNameArray
  }
}

object CarNameBitAuto {

  def main(args: Array[String]): Unit ={
    val test = (new CarNameBitAuto).get("http://baa.bitauto.com/foruminterrelated/brandforumlist_by_pinpai.html")

    val space = " "
    val space2 = " "
    for(tt <- test){
      println(tt._1 + tt._1.length)
      if(tt._1.contains(' '))println("space" + tt._1.replace(space, "").replace(space2, ""))
      for(t <- tt._2){
        print(t)
      }
      println
    }



  }
}
