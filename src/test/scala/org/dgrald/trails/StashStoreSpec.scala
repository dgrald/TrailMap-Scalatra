package org.dgrald.trails

import java.util.UUID

import org.dgrald.{AnyRandom, TestDatabase}
import org.specs2.mutable._

/**
  * Created by dylangrald on 6/22/16.
  */
class StashStoreSpec extends Specification {

  val database = TestDatabase.connection("test-stash-db-will-be-dropped")
  val db = StashStore.apply(database)

  "Adding a new trail point should add it to the database correctly" in {
    val newTrailName = "Test"
    val newTrailLat = 22.22
    val newTrailLong = 33.33
    val newTrailId = UUID.randomUUID().toString
    val newTrail = new Stash(newTrailId, "Test", Location(longitude = newTrailLong, latitude = newTrailLat))
    db.saveTrail(newTrail)

    val addedTrail = db.getTrail(newTrailId).get
    addedTrail.name must_== newTrailName
    addedTrail.id must_== newTrailId
    addedTrail.location match {
      case point: PointLocation =>
        point.longitude must_== newTrailLong
        point.latitude must_== newTrailLat
    }

  }

  "Adding a new stash line should add it to the database correctly" in {
    val newTrailName = AnyRandom.string()
    val trailLine = List((1.0,1.0), (2.0,2.0), (3.0,3.0), (4.0,4.0))
    val trailLocation = Location(trailLine)
    val newTrailId = UUID.randomUUID().toString
    val newTrail = new Stash(newTrailId, newTrailName, trailLocation)

    db.saveTrail(newTrail)

    val addedTrail = db.getTrail(newTrailId).get

    addedTrail.name must_== newTrailName
    addedTrail.id must_== newTrailId
    addedTrail.location match {
      case line: LineLocation =>
        line.linePoints must_== trailLine
    }
  }

  "Updating an existing line trail should update the correct fields" in {
    val originalTrailName = "Test"
    val originalTrailLine = List((1.0,1.0), (2.0,2.0), (3.0,3.0), (4.0,4.0))
    val trailId = UUID.randomUUID().toString
    db.saveTrail(new Stash(trailId, originalTrailName, Location(originalTrailLine)))

    val newTrailName = "Test 2"
    val newTrailLat = 55.55
    val newTrailLong = 66.66

    db.updateTrail(new Stash(trailId, newTrailName, Location(longitude = newTrailLong, latitude = newTrailLat)))

    val updatedTrail = db.getTrail(trailId).get
    updatedTrail.name must_== newTrailName
    updatedTrail.id must_== trailId
    updatedTrail.location match {
      case point: PointLocation =>
        point.longitude must_== newTrailLong
        point.latitude must_== newTrailLat
    }
  }

  "Updating an existing point trail should update the correct fields" in {
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
    updatedTrail.name must_== newTrailName
    updatedTrail.id must_== trailId
    updatedTrail.location match {
      case point: PointLocation =>
        point.longitude must_== newTrailLong
        point.latitude must_== newTrailLat
    }
  }

  "Deleting an existing trail should remove the trail from the database" in {
    val originalTrailName = "Test"
    val originalTrailLat = 22.22
    val originalTrailLong = 33.33
    val trailId = UUID.randomUUID().toString
    val trail = new Stash(trailId, originalTrailName, Location(longitude = originalTrailLong, latitude = originalTrailLat))
    db.saveTrail(trail)

    db.deleteTrail(trail)

    db.getTrail(trailId) must_== None
  }
}
