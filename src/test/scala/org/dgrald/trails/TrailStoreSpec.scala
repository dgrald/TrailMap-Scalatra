package org.dgrald.trails

import java.util.UUID

import com.mongodb.casbah.Imports._
import org.specs2.mutable._
import org.specs2.specification.BeforeEach

/**
  * Created by dylangrald on 6/22/16.
  */
class TrailStoreSpec extends Specification with BeforeEach {

  val mongoClient = MongoClient("localhost", 27017)
  val database = mongoClient("test-db-will-be-dropped")
  lazy val db = TrailStore.apply(database)

  def before = {
    database.dropDatabase()
  }

  "Adding a new trail should add it to the database correctly" in {
    val newTrailName = "Test"
    val newTrailLat = 22.22
    val newTrailLong = 33.33
    val newTrailId = UUID.randomUUID().toString
    val newTrail = new Trail(newTrailId, "Test", new Location(longitude = newTrailLong, latitude = newTrailLat))
    db.saveTrail(newTrail)

    val addedTrail = db.getTrail(newTrailId).get
    addedTrail.name must_== newTrailName
    addedTrail.id must_== newTrailId
    addedTrail.location.longitude must_== newTrailLong
    addedTrail.location.latitude must_== newTrailLat
  }

  "Updating an existing trail should update the correct fields" in {
    val originalTrailName = "Test"
    val originalTrailLat = 22.22
    val originalTrailLong = 33.33
    val trailId = UUID.randomUUID().toString
    db.saveTrail(new Trail(trailId, originalTrailName, new Location(longitude = originalTrailLong, latitude = originalTrailLat)))

    val newTrailName = "Test 2"
    val newTrailLat = 55.55
    val newTrailLong = 66.66

    db.updateTrail(new Trail(trailId, newTrailName, new Location(longitude = newTrailLong, latitude = newTrailLat)))

    val updatedTrail = db.getTrail(trailId).get
    updatedTrail.name must_== newTrailName
    updatedTrail.id must_== trailId
    updatedTrail.location.longitude must_== newTrailLong
    updatedTrail.location.latitude must_== newTrailLat
  }

  "Deleting an existing trail should remove the trail from the database" in {
    val originalTrailName = "Test"
    val originalTrailLat = 22.22
    val originalTrailLong = 33.33
    val trailId = UUID.randomUUID().toString
    val trail = new Trail(trailId, originalTrailName, new Location(longitude = originalTrailLong, latitude = originalTrailLat))
    db.saveTrail(trail)

    db.deleteTrail(trail)

    db.getTrail(trailId) must_== None
  }
}
