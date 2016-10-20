package org.dgrald.trails

import org.json4s._
import org.json4s.jackson._
import org.json4s.JsonDSL._

/**
  * Created by dylangrald on 10/19/16.
  */
abstract class JsonConverter {
  def getTrailFromJson(requestJson: String): Option[Stash]

  def createTrailJson(trail: Stash): JsonAST.JObject

}

object JsonConverter {
  def apply(): JsonConverter = new JsonConverterImplementation
}

private class JsonConverterImplementation extends JsonConverter {
  implicit val jsonFormats = DefaultFormats

  override def getTrailFromJson(requestJson: String): Option[Stash] = {
    val parsedJson = parseJson(requestJson)
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

  override def createTrailJson(trail: Stash): JsonAST.JObject = {
    def createLocationJson(trail: Stash) = {
      trail.location match {
        case point: PointLocation => "location" -> (("longitude" -> point.longitude) ~ ("latitude" -> point.latitude))
      }
    }

    ("id" -> trail.id) ~ ("name" -> trail.name) ~ createLocationJson(trail)
  }
}
