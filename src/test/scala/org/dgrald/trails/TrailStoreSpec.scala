package org.dgrald.trails

import java.util.UUID

import com.mongodb.casbah.Imports._
import org.specs2.mutable._

/**
  * Created by dylangrald on 6/22/16.
  */
class TrailStoreSpec extends Specification {

  "Adding a new trail must add it to the database correctly" in new TestDatabase {
    val newTrailName = "Test"
    val newTrailLat = 22.22
    val newTrailLong = 33.33
    val newTrailId = UUID.randomUUID()
    db.saveTrail(new Trail(newTrailId, "Test", new Location(longitude = newTrailLong, latitude = newTrailLat)))

    val allTrails = db.getTrails
    allTrails must have size(1)

    val addedTrail = allTrails.head
    addedTrail.name must_== newTrailName
    addedTrail.id must_== newTrailId
    addedTrail.location.longitude must_== newTrailLong
    addedTrail.location.latitude must_== newTrailLat
  }
}

trait TestDatabase extends Before {
  val mongoClient = MongoClient("localhost", 27017)
  val database = mongoClient("test")
  lazy val db = TrailStore.apply(database)

  def before = {
    database.dropDatabase()
  }
}
