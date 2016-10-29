package org.dgrald

import scala.util.Random

/**
  * Created by dylangrald on 7/11/16.
  */
object AnyRandom {
  val random = new Random()

  def decimal(): Double = {
    random.nextDouble()
  }

  def string(): String = {
    (for {
      _ <- 1 to 10
    } yield random.alphanumeric).mkString("")
  }
}
