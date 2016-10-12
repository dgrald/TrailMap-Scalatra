package org.dgrald.trails

/**
  * Created by dylangrald on 7/11/16.
  */
trait Location

object Location {
  def apply(longitude: Double, latitude: Double): Location = new PointLocation(longitude, latitude)
}

case class PointLocation(longitude: Double, latitude: Double) extends Location