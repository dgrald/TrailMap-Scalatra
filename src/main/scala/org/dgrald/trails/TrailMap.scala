package org.dgrald.trails

import org.scalatra._
import org.scalatra.json._

import org.json4s._
import org.json4s.JsonDSL._

class TrailMap(trailStore: TrailStore) extends TrailMapStack with JacksonJsonSupport {

  implicit val jsonFormats = DefaultFormats

  post("/trails") {
    val trailOption = getTrailFromJson(request.body)
    trailOption match {
      case Some(validTrail) => {
        val savedTrail = trailStore.saveTrail(validTrail)
        createTrailJson(savedTrail)
      }
      case _ => BadRequest()
    }
  }

  get("/trails/:id") {
    val id = params("id")
    val trailOption = trailStore.getTrail(id)
    trailOption match {
      case Some(trail) => createTrailJson(trail)
      case None => NotFound()
    }
  }

  get("/trails") {
    val allTrails = trailStore.getTrails
    val trailJson = for {
      trail <- allTrails
    } yield createTrailJson(trail)

    new JArray(trailJson.toList)
  }

  private def createTrailJson(trail: Trail) = {
    ("name" -> trail.name) ~ createLocationJson(trail)
  }

  private def createLocationJson(trail: Trail) = {
    "location" -> (("longitude" -> trail.location.longitude) ~ ("latitude" -> trail.location.latitude))
  }

  private def getTrailFromJson(requestJson: String): Option[Trail] = {
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
    Some(new Trail(nameValue, new Location(longitude.extract[String].toDouble, latitude.extract[String].toDouble)))
  }
}
