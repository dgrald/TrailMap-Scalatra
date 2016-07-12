package org.dgrald.trails

/**
  * Created by dylangrald on 7/11/16.
  */
sealed class Location(val longitude: Double, val latitude: Double) {
  def canEqual(other: Any): Boolean = other.isInstanceOf[Location]

  override def equals(other: Any): Boolean = other match {
    case that: Location =>
      (that canEqual this) &&
        longitude == that.longitude &&
        latitude == that.latitude
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(longitude, latitude)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}