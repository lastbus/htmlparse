package com.shgc.htmlparse.parse

import java.util.regex.Pattern

import com.shgc.htmlparse.util.ParseConfiguration

import scala.collection.mutable

/**
 * Created by Administrator on 2015/11/19.
 */
object ParserFactory extends Serializable{
  private[this] var forumParser: Qy58Parser = null
  private[this] var autoHomeParser: AutoHomeParser = null

  def getParser(className: String): Parser ={
    if(className.equalsIgnoreCase("forumParser"))
      return getQy58Parser()
    else if(className.equalsIgnoreCase("autohomeparser")){
      return getAutoHomeParser()
    }else null
  }



  def getQy58Parser(): Parser ={
    if (forumParser == null) {
      forumParser = new Qy58Parser
      forumParser.urlMap = ParseConfiguration.urlMap.toMap
      forumParser
    } else forumParser
  }


  def getAutoHomeParser(): Parser ={
    if (autoHomeParser == null) {
      autoHomeParser = new AutoHomeParser
      autoHomeParser.urlMap = ParseConfiguration.urlMap.toMap
      autoHomeParser
    } else autoHomeParser
  }

  def createParseMap(): Map[Pattern, Parser] ={
    val autoHome = Pattern.compile("http://club\\.autohome\\.com\\.cn/bbs/thread-[]a-z]+-\\d+-\\d+-\\d+\\.htm[l]*")
    val bitAuto = Pattern.compile("http://baa\\.bitauto\\.com/\\w+/thread-\\d+[-\\d+]*.htm[l]*")
    val pCAuto = Pattern.compile("http://bbs.pcauto.com.cn/forum-.*")
    val xCar = Pattern.compile("http://www\\.xcar\\.com\\.cn/bbs/viewthread\\.php\\?tid=\\d+[(\\&page=\\d+)]*")
    val xinLang = Pattern.compile("http://bbs\\.auto\\.sina\\.com\\.cn/\\d+[/\\d+]*/thread-\\d+-\\d+-\\d+\\.htm[l]*")
    val soHu = Pattern.compile("http://saa.auto.sohu.com/[a-z]+/thread-\\d+-\\d+.[s]html")
    val QQ = Pattern.compile("http://club\\.auto\\.qq\\.com/t-\\d+-\\d+\\.htm[l]*")

    val parseMap = mutable.HashMap.empty[Pattern, Parser]

    parseMap(autoHome) = new AutoHomeParser
    parseMap(bitAuto) = new BitAutoParser
    parseMap(pCAuto) = new PCAutoParser
    parseMap(xCar) = new XCarParser
    parseMap(xinLang) = new XinLangParser
    parseMap(soHu) = new SoHuParse
    parseMap(QQ) = new QQParse

    parseMap.toMap
  }
}
