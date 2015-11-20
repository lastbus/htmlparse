package com.shgc.htmlparse.parse

import com.shgc.htmlparse.util.ParseConfiguration

/**
 * Created by Administrator on 2015/11/19.
 */
object ParserFactory extends Serializable{
  private[this] var forumParser: Qy58Parser = null

  def getParser(className: String): Parser ={
    if(className.equalsIgnoreCase("forumParser"))
      if (forumParser == null) {
        forumParser = new Qy58Parser
        forumParser.urlMap = ParseConfiguration.urlMap.toMap
        forumParser
      } else forumParser
    else null
  }
}
