package org.dgrald.trails

import org.dgrald.AnyRandom
import org.specs2.mutable.Specification

/**
  * Created by dylangrald on 7/11/16.
  */
class LocationSpec extends Specification {

  "Two locations with the same point should be equal" in {
    val longitude = AnyRandom.decimal()
    val latitude = AnyRandom.decimal()

    val location1 = Location(longitude, latitude)
    val location2 = Location(longitude, latitude)

    location1 must_== location2
  }

  "Two locations that are not the same point should not be equal" in {
    val longitude = AnyRandom.decimal()
    val latitude = AnyRandom.decimal()

    val location1 = Location(longitude, latitude)
    val location2 = Location(longitude, latitude + 1)
    val location3 = Location(longitude + 1, latitude)

    location1 must_!= location2
    location1 must_!= location2
    location2 must_!= location3
  }
}
