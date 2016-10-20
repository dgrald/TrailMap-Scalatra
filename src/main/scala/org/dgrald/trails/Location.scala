package org.dgrald.trails

/**
  * Created by dylangrald on 7/11/16.
  */
trait Location

object Location {
  def apply(longitude: Double, latitude: Double): Location = new PointLocation(longitude, latitude)

  def apply(linePoints: List[(Double, Double)]): Location = new LineLocation(linePoints)
}

case class PointLocation(longitude: Double, latitude: Double) extends Location

case class LineLocation(linePoints: List[(Double, Double)]) extends Location