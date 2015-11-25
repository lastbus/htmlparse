package com.shgc.htmlparseTest.util

import com.shgc.htmlparse.util.ParseConfiguration
import org.junit.Test

/**
 * Created by Administrator on 2015/11/24.
 */
@Test
class ConfTest {

  @Test
  def run = {
    ParseConfiguration.readConf()

    for((url, se) <- ParseConfiguration.urlMap){
      println(url)
    }



  }
}
