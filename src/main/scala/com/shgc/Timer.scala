package com.shgc

/**
 * Created by Administrator on 2015/12/8.
 */
object Timer {
  def main(args: Array[String]): Unit ={
    oncePerSecond(timeFlies)
  }

  def timeFlies(): Unit ={
    println("time flies like an arrow...")
  }

  def oncePerSecond(callback: () => Unit): Unit ={
    while (true){callback(); Thread sleep 1000}
  }
}
