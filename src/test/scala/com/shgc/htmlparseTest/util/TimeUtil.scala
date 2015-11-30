package com.shgc.htmlparseTest.util

import com.shgc.htmlparse.util.TimeUtil
import org.junit.Test

/**
 * Created by Administrator on 2015/11/26.
 */
@Test
class TimeUtil {

//  @Test
  def run = {
    val s = "注册：2009年9月11日   首页游记大牛一级勋章 使用“发游记”发表游" +
      "记并被首页推荐，就会为热爱旅行的你颁发“首页游记大牛一级勋章”。   优秀追加口" +
      "碑 发表追加口碑，评价500字以上，通过编辑审核为优质追加口碑，特此奖励优秀追加口碑" +
      "勋章，以示鼓励。   进口三菱转动南半球勋章 穿越赤道，飞越太平洋，奔向南半球。逃" +
      "离世俗与繁杂，感受速度与激情。世界再大，有你的足迹！   全系捷豹XE 领跑者 捷豹" +
      "品牌全新运动轿跑XE将于广州车展亮相，即日起参与注册即有机会赢取精美好礼！   路虎" +
      "第四代发现25周年 共同为路虎第四代发现25周年庆生，用纯正英伦越野血统带你从容跨越" +
      "山水之间！"
    val s2 = "看见的防控等级考试 健康的放量加速扩大"
    val s3 = ""
    val s4 = null
    val time = TimeUtil.getAutoHomeRT(s)
    println(time)
//    println(TimeUtil.getAutoHomeRT(s2))
//    println(TimeUtil.getAutoHomeRT(s3))
//    println(TimeUtil.getAutoHomeRT(s4))

  }

  @Test
  def getPostTime = {
    val a = "妨碍鳌鱼 2013-43-43 88:99:00"
    val b = "2011-11-11 99:99:09"
    val c = ".18011801EE22"
    println(TimeUtil.getPostTime(a))
    println(TimeUtil.getPostTime(b))
    println(TimeUtil.getPostTime(c))
    println(TimeUtil.extractTimeString(c))

  }
}
