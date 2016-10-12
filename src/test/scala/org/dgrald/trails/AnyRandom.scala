package org.dgrald.trails

import scala.util.Random

/**
  * Created by dylangrald on 7/11/16.
  */
object AnyRandom {
  def decimal(): Double = {
    new Random().nextDouble()
  }
}
