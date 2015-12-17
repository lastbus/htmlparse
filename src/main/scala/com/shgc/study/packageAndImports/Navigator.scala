package com.shgc.study.packageAndImports



/**
 * Created by Administrator on 2015/12/17.
 */
package bobsrockets
package navigation {

private[bobsrockets] class Navigator {

  protected[navigation] def useStarChart(): Unit = {
    val legOfJourney = new LegOfJourney
    println("legOfJourney " + legOfJourney.distance)
  }

  def print(): Unit = {
    println("hh")
  }

  class LegOfJourney {
    private[Navigator] val distance = 100
  }

  private[this] var speed = 200
}

}

package launch {

import navigation._

object Vehicle {
  private[launch] val guide = new Navigator

  def main(args: Array[String]): Unit = {
    guide.print()


  }
}

  class TestVehicle {
    Vehicle.guide
  }
}

