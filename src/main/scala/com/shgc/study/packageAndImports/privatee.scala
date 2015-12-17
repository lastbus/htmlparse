package com.shgc.study.packageAndImports

/**
 * Created by Administrator on 2015/12/17.
 */
class privatee {

  class Outer{
    private def printa: Unit ={
      println("i'm in outer class")
    }
    class Inner{
      printa
    }
  }

  // wrong
//  (new Outer).printa

}
