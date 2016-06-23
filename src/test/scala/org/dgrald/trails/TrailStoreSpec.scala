package org.dgrald.trails

import com.mongodb.casbah.Imports._
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
  val mongoClient = MongoClient("localhost", 27017)
  val database = mongoClient("test")
  lazy val db = TrailStore.apply(database)

  def before = {
    database.dropDatabase()
  }
}
