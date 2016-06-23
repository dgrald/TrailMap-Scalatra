package org.dgrald.trails

import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoDatabase, MongoClient}

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
    val mongoClient: MongoClient = MongoClient()
    val database: MongoDatabase = mongoClient.getDatabase("production")
    new TrailStoreImplementation(database)
  }

  def apply(database: MongoDatabase): TrailStore = {
    new TrailStoreImplementation(database)
  }
}

private class TrailStoreImplementation(database: MongoDatabase) extends TrailStore {

  val trailsCollection = database.getCollection("trails")

  override def getTrails: Seq[Trail] = ???

  override def getTrail(id: String): Option[Trail] = ???

  override def saveTrail(trail: Trail): Trail = {
    trailsCollection.insertOne(Document())
    trail
  }

  override def deleteTrail(trail: Trail): Unit = ???
}

sealed class Trail(val name: String, val location: Location)

sealed class Location(val longitude: Double, val latitude: Double)