package com.shgc.htmlparse.parse

import java.util.regex.Pattern

import com.shgc.forumName.LoadCarBandName
import com.shgc.htmlparse.util.ParseConfiguration

import scala.collection.mutable

/**
 * Created by make on 2015/11/19.
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

    val autoHome = Pattern.compile("http://club.autohome.com.cn/bbs/thread-[a-z]+-\\d+-\\d+-\\d+.htm[l]*")
    val bitAuto = Pattern.compile("http://baa.bitauto.com/\\w+/thread-\\d+[-\\d+]*.htm[l]*")
    val pCAuto = Pattern.compile("http://bbs.pcauto.com.cn/topic-\\d+[-(\\d+)]*.htm[l]*")
    val xCar = Pattern.compile("http://www.xcar.com.cn/bbs/viewthread.php\\?tid=[0-9]+[(&page=[0-9]+)]*")
    val xinLang = Pattern.compile("http://bbs.auto.sina.com.cn/[0-9]+(/[0-9]+)*/thread-[0-9]+-[0-9]+-[0-9]+.htm[l]*")
    val soHu = Pattern.compile("http://saa.auto.sohu.com/[a-z]+/thread-[0-9]+-[0-9]+.[s]*htm[l]*")
    val QQ = Pattern.compile("http://club.auto.qq.com/t-[0-9]+-[0-9]+.htm[l]*")

    val parseMap = mutable.HashMap.empty[Pattern, Parser]

    val autoHomeParse = new AutoHomeParser
    autoHomeParse.vehicleBandMap = LoadCarBandName.loadAutoHome
    parseMap(autoHome) = autoHomeParse

    val bitAutoParse = new BitAutoParser
    bitAutoParse.vehicleAndType = LoadCarBandName.loadBitAuto
    parseMap(bitAuto) = bitAutoParse

    val pcAutoParse = new PCAutoParser
    pcAutoParse.vehicleBandAndType = LoadCarBandName.loadPcAuto
    parseMap(pCAuto) = pcAutoParse

    val xCarParse = new XCarParser
    xCarParse.vehicleBandAndCarType = LoadCarBandName.loadXCar
    parseMap(xCar) = xCarParse

    val xinLangParse = new XinLangParser
    xinLangParse.vehicleBandAndCarType = LoadCarBandName.loadSina
    parseMap(xinLang) = xinLangParse

    val soHuParse = new SoHuParse
    soHuParse.vehicleBandAndCarType = LoadCarBandName.loadSoHu
    parseMap(soHu) = soHuParse

    parseMap(QQ) = new QQParse

    parseMap.toMap
  }
}
