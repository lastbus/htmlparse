package com.shgc.proxy

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by make on 2015/12/18.
 */
class TimerProxy(val task: Task) extends Proxy{

  override def execute(args: Array[String]): Int = {
    val timeStart = new Date()
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    println(s"===#### ${task.getClass.getName} begins at: ${sdf.format(timeStart)} ###===")
    val r = try {
      task.run(args)
    }catch {
      case _:Exception => {println("error in execution");-1}
    }
    println("\ntime taken: " + (new Date().getTime - timeStart.getTime) / 1000 + " seconds\n\n")
    r
  }

}
