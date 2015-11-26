package com.shgc.htmlparseTest.util

import com.shgc.htmlparse.util.FloorUtil
import org.junit.Test

/**
 * Created by Administrator on 2015/11/26.
 */
@Test
class FloorUtils {

  @Test
  def getFloorNumber = {
    val a = "楼主"
    val b = "1#"
    val c = "5楼"
    assert("0" == FloorUtil.getFloorNumber(a))
    assert("1" == FloorUtil.getFloorNumber(b))
    assert("5" == FloorUtil.getFloorNumber(c))
  }
}
