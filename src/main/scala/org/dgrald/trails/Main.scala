package org.dgrald.trails

import java.util.UUID

import com.mongodb.casbah.Imports._

/**
  * Created by dylangrald on 10/24/16.
  */
object Main {
  def main(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)
    val database = mongoClient("test-db-will-be-dropped")
    val db = StashStore.apply(database)

    val originalTrailName = "Test"
    val originalTrailLat = 22.22
    val originalTrailLong = 33.33
    val trailId = UUID.randomUUID().toString
    db.saveTrail(new Stash(trailId, originalTrailName, Location(longitude = originalTrailLong, latitude = originalTrailLat)))

    val newTrailName = "Test 2"
    val newTrailLat = 55.55
    val newTrailLong = 66.66

    db.updateTrail(new Stash(trailId, newTrailName, Location(longitude = newTrailLong, latitude = newTrailLat)))

    val updatedTrail = db.getTrail(trailId).get
    val x = updatedTrail.name
  }

}
