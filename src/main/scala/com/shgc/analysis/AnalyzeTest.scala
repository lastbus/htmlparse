package com.shgc.analysis

import com.shgc.excel.ReadExcel

/**
 * Created by Administrator on 2015/12/24.
 */
object AnalyzeTest {

  def main(args: Array[String]): Unit = {
    val names = Array(("产品关注点", 3), ("服务关注点", 2), ("品牌关注点", 3))
    val names2 = Array(("车主对产品的满意点", 3), ("车主对产品的抱怨点", 3))
    val readExcel = new ReadExcel
    val data = readExcel.read("E:2015年长安乘用车BTS-乘用车词库-1120.xlsx", names)
    val data2 = readExcel.read("E:2015年长安乘用车BTS-乘用车词库-1120.xlsx", names2)

    val d = Analyze.mapArray2Map(data)
    val d2 = Analyze.mapArray2Map(data2)

    val comment = "引用: 原帖由 宝贝牛牛0722 于 2011-03-3016:37 发表在 4 楼 机舱盖贴了隔音棉在驾驶室还能感觉到发动机的震动吗？" +
      "我的车什么都没改，坐在驾驶室在车怠速的时感觉到发动机的震动，好烦 你车质量下降了！杯具了年的车，怠速几乎感觉不到"
    val r = Analyze.oneFunction("a", comment, d, d2)
    if(r != null){
      for(s <- r){
        println(s"${s._1}   ${s._2}   ${s._3}")
      }
    } else {
      println("no result")
    }

  }

}
