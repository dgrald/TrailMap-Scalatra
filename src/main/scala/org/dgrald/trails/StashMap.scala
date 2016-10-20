package org.dgrald.trails

import org.scalatra._
import org.scalatra.json._

import org.json4s._

class StashMap(stashStore: StashStore, jsonConverter: JsonConverter) extends StashMapStack with JacksonJsonSupport {

  implicit val jsonFormats = DefaultFormats

  post("/trails") {
    persistTrail(stashStore.saveTrail)
  }

  put("/trails") {
    persistTrail(stashStore.updateTrail)
  }

  get("/trails/:id") {
    val id = params("id")
    val trailOption = stashStore.getTrail(id)
    trailOption match {
      case Some(trail) => jsonConverter.createTrailJson(trail)
      case None => NotFound(s"Could not find a trail with the ID $id")
    }
  }

  get("/trails") {
    val allTrails = stashStore.getTrails
    val trailJson = for {
      trail <- allTrails
    } yield jsonConverter.createTrailJson(trail)

    new JArray(trailJson.toList)
  }

  delete("/trails/:id") {
    val id = params("id")
    val trailOption = stashStore.getTrail(id)
    trailOption match {
      case Some(trail) => {
        stashStore.deleteTrail(trail)
        NoContent()
      }
      case None => NotFound(s"Could not find a trail with the ID ${id}")
    }
  }

  private def persistTrail(persistenceFunction: (Stash) => Stash) = {
    val trailOption = jsonConverter.getTrailFromJson(request.body)
    trailOption match {
      case Some(validTrail) => {
        val savedTrail = persistenceFunction(validTrail)
        jsonConverter.createTrailJson(savedTrail)
      }
      case _ => BadRequest()
    }
  }
}
