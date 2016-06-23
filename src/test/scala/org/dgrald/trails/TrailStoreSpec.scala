package org.dgrald.trails

import org.mongodb.scala.{MongoDatabase, MongoClient}
import org.specs2.mutable._

/**
  * Created by dylangrald on 6/22/16.
  */
class TrailStoreSpec extends Specification {

  "Adding a new trail must add it to the database correctly" in new TestDatabase {
    db.saveTrail(new Trail("Test", new Location(22.22, 22.22)))

    val allTrails = db.getTrails
    allTrails must have size(1)
  }
}

trait TestDatabase extends Before {
  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("test")
  lazy val db = TrailStore.apply(database)

  def before = {
    db.getTrails.foreach(t => db.deleteTrail(t))
  }
}
