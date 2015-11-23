package com.shgc.htmlparse.parse

import com.shgc.htmlparse.util.ParseConfiguration

/**
 * Created by Administrator on 2015/11/19.
 */
object ParserFactory extends Serializable{
  private[this] var forumParser: Qy58Parser = null
  private[this] var autoHomeParser: AutoHomeParser2 = null

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
      autoHomeParser = new AutoHomeParser2
      autoHomeParser.urlMap = ParseConfiguration.urlMap.toMap
      autoHomeParser
    } else autoHomeParser
  }
}
