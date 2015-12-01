package com.shgc.htmlparseTest.util

import org.jsoup.Jsoup
import org.junit.Test

/**
 * Created by Administrator on 2015/12/1.
 */
@Test
class ggg {

  @Test
  def test ={
    val s = "<div class=\"rconten\"> \n " +
      "<div class=\"plr26 rtopconnext\"> \n  " +
      "<div class=\"fr\"> \n   " +
      "<a title=\"复制本楼链接到剪贴板\" class=\"rightbutlz\" href=\"#\" rel=\"3\">地板</a> \n " +
      " </div> \n  " +
      "<span>发表于 </span> \n  " +
      "<span xname=\"date\">2014-10-21 09:01:02</span> \n </div> \n <div class=\"x-reply font14\" xname=\"content\"> \n  " +
      "<div class=\"w740\"> \n   " +
      "<img src=\"http://www.autoimg.cn/Album/kindeditor/smiles/44.gif\"> \n  " +
      "</div> \n </div> \n</div>"
    val e = Jsoup.parse(s)
    println()
    println(e.text().indexOf("发表于33"))


  }
}
