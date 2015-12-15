package com.shgc.forumName

/**
 * Created by Administrator on 2015/12/15.
 */
trait GetCarName {

  def get(url: String): Array[(String, Array[String])]

}
