package org.dgrald.trails

import org.scalatra._
import org.scalatra.json._

import org.json4s._
import org.json4s.JsonDSL._

class StashMap(stashStore: StashStore) extends StashMapStack with JacksonJsonSupport {

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
      case Some(trail) => createTrailJson(trail)
      case None => NotFound(s"Could not find a trail with the ID $id")
    }
  }

  get("/trails") {
    val allTrails = stashStore.getTrails
    val trailJson = for {
      trail <- allTrails
    } yield createTrailJson(trail)

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

  private def createTrailJson(trail: Stash) = {
    ("id" -> trail.id) ~ ("name" -> trail.name) ~ createLocationJson(trail)
  }

  private def createLocationJson(trail: Stash) = {
    trail.location match {
      case point: PointLocation => "location" -> (("longitude" -> point.longitude) ~ ("latitude" -> point.latitude))
    }
  }

  private def getTrailFromJson(requestJson: String): Option[Stash] = {
    val parsedJson = parse(requestJson)
    val name = parsedJson \ "name"

    if(name == JNothing) {
      return None
    }
    val nameValue = name.extract[String]
    val location = parsedJson \ "location"

    if(location == JNothing) {
      return None
    }

    val longitude = location \ "longitude"
    val latitude = location \ "latitude"
    if(longitude == JNothing || latitude == JNothing) {
      return None
    }
    Some(Stash(nameValue, Location(longitude.extract[String].toDouble, latitude.extract[String].toDouble)))
  }

  private def persistTrail(persistenceFunction: (Stash) => Stash) = {
    val trailOption = getTrailFromJson(request.body)
    trailOption match {
      case Some(validTrail) => {
        val savedTrail = persistenceFunction(validTrail)
        createTrailJson(savedTrail)
      }
      case _ => BadRequest()
    }
  }
}
