package org.dgrald.trails

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._

/**
  * Created by dylangrald on 6/21/16.
  */
trait StashStore {
  def getTrails: Seq[Stash]

  def getTrail(id: String): Option[Stash]

  def saveTrail(trail: Stash): Stash

  def deleteTrail(trail: Stash): Unit

  def updateTrail(trail: Stash): Stash
}

object StashStore {
  def apply(): StashStore = {
    val mongoClient = MongoClient(MongoClientURI("mongodb://admin:password123@ds059306.mlab.com:59306/heroku_dqm6trvw"))
    val database = mongoClient("heroku_dqm6trvw")
    new StashStoreImplementation(database)
  }

  def apply(database: MongoDB): StashStore = {
    new StashStoreImplementation(database)
  }
}

private class StashStoreImplementation(database: MongoDB) extends StashStore {

  val trailsCollection = database("trails")

  override def getTrails: Seq[Stash] = {
    val cursor = trailsCollection.find
    val allTrails = for {
      next <- cursor
    } yield convertToTrail(next)
    allTrails.toList
  }

  override def getTrail(id: String): Option[Stash] = {
    val findByIdOption = trailsCollection.findOneByID(id)
    findByIdOption match {
      case Some(matchingTrail) => Some(convertToTrail(matchingTrail))
      case None => None
    }
  }

  override def saveTrail(trail: Stash): Stash = {
    trailsCollection.insert(convertToDBObject(trail))
    trail
  }

  override def deleteTrail(trail: Stash): Unit = {
    trailsCollection.remove(MongoDBObject("_id" -> trail.id))
  }

  override def updateTrail(trail: Stash): Stash = {
    val builder = trailsCollection.initializeOrderedBulkOperation
    val writeRequestBuilder = builder.find(MongoDBObject("_id" -> trail.id))
    writeRequestBuilder.updateOne($set("name" -> trail.name))
    trail.location match {
      case point: PointLocation => {
        writeRequestBuilder.updateOne($set("location.longitude" -> point.longitude))
        writeRequestBuilder.updateOne($set("location.latitude" -> point.latitude))
      }
    }

    builder.execute()

    trail
  }

  private def convertToDBObject(trail: Stash) = {
    trail.location match {
      case point: PointLocation => {
        MongoDBObject("_id" -> trail.id, "name" -> trail.name) ++ ("location" -> MongoDBObject("longitude" -> point.longitude, "latitude" -> point.latitude))
      }
    }
  }

  private def findTrail(trail: Stash): MongoCursor = {
    val toMatch: DBObject = MongoDBObject("_id" -> trail.id)
    trailsCollection.find(toMatch)
  }

  private def convertToTrail(next: Imports.DBObject): Stash = {
    val name = next.getAs[String]("name").get
    val locationMap = next.getAs[Map[String, Double]]("location").get
    val id = next.getAs[String]("_id").get
    new Stash(id, name, Location(latitude = locationMap("latitude"), longitude = locationMap("longitude")))
  }
}