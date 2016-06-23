package org.dgrald.trails

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._

/**
  * Created by dylangrald on 6/21/16.
  */
trait TrailStore {
  def getTrails: Seq[Trail]

  def getTrail(id: String): Option[Trail]

  def saveTrail(trail: Trail): Trail

  def deleteTrail(trail: Trail): Unit
}

object TrailStore {
  def apply(): TrailStore = {
    val mongoClient = MongoClient("localhost", 27017)
    val database = mongoClient("production")
    new TrailStoreImplementation(database)
  }

  def apply(database: MongoDB): TrailStore = {
    new TrailStoreImplementation(database)
  }
}

private class TrailStoreImplementation(database: MongoDB) extends TrailStore {

  val trailsCollection = database("trails")

  override def getTrails: Seq[Trail] = {
    val cursor = trailsCollection.find
    val allTrails = for {
      next <- cursor
    } yield convertToTrail(next)
    allTrails.toList
  }

  override def getTrail(id: String): Option[Trail] = ???

  override def saveTrail(trail: Trail): Trail = {
    trailsCollection.insert(MongoDBObject("name" -> trail.name) ++ ("location" -> MongoDBObject("longitude" -> trail.location.longitude, "latitude" -> trail.location.latitude)))
    trail
  }

  override def deleteTrail(trail: Trail): Unit = ???

  private def convertToTrail(next: Imports.DBObject): Trail = {
    val name = next.getAs[String]("name").get
    val locationMap = next.getAs[Map[String, Double]]("location").get
    new Trail(name, new Location(latitude = locationMap("latitude"), longitude = locationMap("longitude")))
  }
}

sealed class Trail(val name: String, val location: Location)

sealed class Location(val longitude: Double, val latitude: Double)