package com.shgc.forumName

import org.jsoup.Jsoup

/**
 * Created by Administrator on 2015/12/16.
 */
class CarNameTencent extends GetCarName{

  /**
   * 注意： 有的车型取出来不符合要求，舍弃，最后一步对结果过滤
   * @param url
   * @return
   */
  override def get(url: String): Array[(String, Array[String])] = {
    val document = Jsoup.connect(url).
      header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0").
      get()
    val lists = document.select("#category_3 table tbody tr")
    val carTypeAndCarNameArray = new Array[(String, Array[String])](lists.size)
    var count = 0
    for(i <- 0 until lists.size){
      val element = lists.get(i).select("td:eq(1)")
      val carType = element.select("h2 a").text().trim
      val carType2 = if(carType.endsWith("汽车")) carType.substring(0, carType.indexOf("汽车")) else carType
      val cars = element.select("p:contains(子版块) a")
      val carsArray = new Array[String](cars.size())
      for(j <- 0 until cars.size()){
        val temp = cars.get(j).text().trim
        carsArray(j) = if(temp.endsWith("车友会")) null else temp
        count += 1
      }
      carTypeAndCarNameArray(i) = (carType2, carsArray.filter(d => d != null && d.length > 0))
    }
    println(s"tencent carType: ${lists.size}")
    println(s"tencent car number: ${count}")
    carTypeAndCarNameArray
  }
}

object CarNameTencent {
  def main(args: Array[String]): Unit ={
    val test = (new CarNameTencent).get("http://club.auto.qq.com/forum.php?gid=3")
    for(tt <- test){
      println(tt._1)
      for(t <- tt._2){
        print(t + "\t")
      }
      println
    }

  }
}
