package org.dgrald

import com.mongodb.casbah.Imports._

/**
  * Created by dylangrald on 10/28/16.
  */
object Database {
  def connection: MongoDB = {
    val mongoClient = MongoClient(MongoClientURI("mongodb://admin:password123@ds031617.mlab.com:31617/heroku_b32xmvk7"))
    mongoClient("heroku_b32xmvk7")
  }
}
