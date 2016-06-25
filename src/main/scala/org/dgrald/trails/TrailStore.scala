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

  def deleteTrail(trail: Trail): Boolean

  def updateTrail(trail: Trail): Trail
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

  override def getTrail(id: String): Option[Trail] = {
    val findByIdOption = trailsCollection.findOneByID(id)
    findByIdOption match {
      case Some(matchingTrail) => Some(convertToTrail(matchingTrail))
      case None => None
    }
  }

  override def saveTrail(trail: Trail): Trail = {
    trailsCollection.insert(convertToDBObject(trail))
    trail
  }

  override def deleteTrail(trail: Trail): Boolean = {
    trailsCollection.remove(MongoDBObject("_id" -> trail.id))
    true
  }

  override def updateTrail(trail: Trail): Trail = {
    val builder = trailsCollection.initializeOrderedBulkOperation
    val writeRequestBuilder = builder.find(MongoDBObject("_id" -> trail.id))
    writeRequestBuilder.updateOne($set("name" -> trail.name))
    writeRequestBuilder.updateOne($set("location.longitude" -> trail.location.longitude))
    writeRequestBuilder.updateOne($set("location.latitude" -> trail.location.latitude))

    builder.execute()

    trail
  }

  private def convertToDBObject(trail: Trail) = {
    MongoDBObject("_id" -> trail.id, "name" -> trail.name) ++ ("location" -> MongoDBObject("longitude" -> trail.location.longitude, "latitude" -> trail.location.latitude))
  }

  private def findTrail(trail: Trail): MongoCursor = {
    val toMatch: DBObject = MongoDBObject("_id" -> trail.id)
    trailsCollection.find(toMatch)
  }

  private def convertToTrail(next: Imports.DBObject): Trail = {
    val name = next.getAs[String]("name").get
    val locationMap = next.getAs[Map[String, Double]]("location").get
    val id = next.getAs[String]("_id").get
    new Trail(id, name, new Location(latitude = locationMap("latitude"), longitude = locationMap("longitude")))
  }

}