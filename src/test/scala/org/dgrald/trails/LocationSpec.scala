package org.dgrald.trails

import org.specs2.mutable.Specification

/**
  * Created by dylangrald on 7/11/16.
  */
class LocationSpec extends Specification {

  "Two locations with the same point should be equal" in {
    val longitude = Some.decimal()
    val latitude = Some.decimal()

    val location1 = new Location(longitude, latitude)
    val location2 = new Location(longitude, latitude)

    location1 must_== location2
  }

  "Two locations that are not the same point should not be equal" in {
    val longitude = Some.decimal()
    val latitude = Some.decimal()

    val location1 = new Location(longitude, latitude)
    val location2 = new Location(longitude, latitude + 1)
    val location3 = new Location(longitude + 1, latitude)

    location1 must_!= location2
    location1 must_!= location2
    location2 must_!= location3
  }
}
