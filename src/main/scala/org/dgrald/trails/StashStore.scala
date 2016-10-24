package org.dgrald.trails

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import org.json4s.jackson.JsonMethods._

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
    val mongoClient = MongoClient(MongoClientURI("mongodb://admin:password123@ds031617.mlab.com:31617/heroku_b32xmvk7"))
    val database = mongoClient("heroku_b32xmvk7")
    new StashStoreImplementation(database)
  }

  def apply(database: MongoDB): StashStore = {
    new StashStoreImplementation(database)
  }
}

private class StashStoreImplementation(database: MongoDB) extends StashStore {

  val jsonConverter = JsonConverter()
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
    val fields = jsonConverter.createTrailJson(trail).values ++ Map("id" -> trail.id, "_id" -> trail.id)
    fields.foreach {
      case (key: String, value: Any) => writeRequestBuilder.update($set(key -> value))
    }

    builder.execute()

    trail
  }

  private def convertToDBObject(trail: Stash) = {
    val baseObject = MongoDBObject("_id" -> trail.id)
    baseObject ++ MongoDBObject(compact(render(jsonConverter.createTrailJson(trail))))
  }

  private def findTrail(trail: Stash): MongoCursor = {
    val toMatch: DBObject = MongoDBObject("_id" -> trail.id)
    trailsCollection.find(toMatch)
  }

  private def convertToTrail(next: Imports.DBObject): Stash = {
    val id = next.getAs[String]("_id").get
    val stash = jsonConverter.getTrailFromJson(next.toString).get
    new Stash(id, stash.name, stash.location)
  }
}