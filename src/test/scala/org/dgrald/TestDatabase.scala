package org.dgrald

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB

/**
  * Created by dylangrald on 10/29/16.
  */
object TestDatabase {
  def connection(name: String): MongoDB = {
    val mongoClient = MongoClient("localhost", 27017)
    mongoClient(name)
  }
}
