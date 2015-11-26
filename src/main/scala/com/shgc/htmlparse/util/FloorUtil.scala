package com.shgc.htmlparse.util

/**
 * Created by Administrator on 2015/11/26.
 */
object FloorUtil {

  def getFloorNumber(floor: String): String ={
    if(floor == null) return null
    else if(floor.equals("楼主")) return "0"
    else if (floor.equals("沙发")) return "1"
    else if (floor.equals("板凳")) return "2"
    else if (floor.equals("地板")) return "3"
      else if(floor.equals("地下室")) return "4"
    else NumExtractUtil.getNumArray(floor)(0)
  }
  def getFloorNumber(floor: String, begin: Int): String ={
    if(floor == null) return null
    else if(floor.contains("楼主")) return "0"
    else if (floor.contains("沙发")) return "1"
    else if (floor.contains("板凳")) return "2"
    else if (floor.contains("地板")) return "3"
    else if(floor.contains("地下室")) return "4"
    else (NumExtractUtil.getNumArray(floor)(0).toInt - begin).toString
  }


}
