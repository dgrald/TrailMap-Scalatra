package org.dgrald.trails

import java.util.UUID

/**
  * Created by dylangrald on 6/23/16.
  */
sealed class Trail(val id: String, val name: String, val location: Location)

object Trail {
  def apply(name: String, location: Location): Trail = {
    new Trail(UUID.randomUUID().toString, name, location)
  }
}

sealed class Location(val longitude: Double, val latitude: Double)
