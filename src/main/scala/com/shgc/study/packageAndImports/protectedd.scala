package com.shgc.study.packageAndImports

/**
 * Created by Administrator on 2015/12/17.
 */
class protectedd {

  class Super{
    protected def myPrintln: Unit = {
      println("I'm super class!")
    }
  }

  class Sub extends Super{
    myPrintln
  }

  //wrong
//  (new Super).myPrintln





}
