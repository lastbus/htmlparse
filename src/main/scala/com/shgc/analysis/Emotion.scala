package com.shgc.analysis

/**
 * Created by Administrator on 2015/12/24.
 */
object Emotion extends Enumeration{

  type Emotion = Value
  val positive = Value("positive")
  val negative = Value("negative")
  val neutral = Value("neutral")
  val notFind = Value("not-found")
}
