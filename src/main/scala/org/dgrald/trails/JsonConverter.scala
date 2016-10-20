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

    location match {
      case JArray(something) =>
        getLineLocation(nameValue, something)
      case _ => getPointLocation(nameValue, location)
    }
  }

  override def createTrailJson(trail: Stash): JsonAST.JObject = {
    def createLocationJson(trail: Stash) = {
      trail.location match {
        case point: PointLocation => "location" -> (("longitude" -> point.longitude) ~ ("latitude" -> point.latitude))
        case line: LineLocation =>
          val points = line.linePoints.map(p => p match {case (first: Double, second: Double) => JArray(List(JDouble(first), JDouble(second)))})
          "location" -> JArray(points)
      }
    }

    ("id" -> trail.id) ~ ("name" -> trail.name) ~ createLocationJson(trail)
  }

  private def getLineLocation(nameValue: String, something: List[JsonAST.JValue]): Some[Stash] = {
    val points: List[(Double, Double)] = something.map(s => s match {
      case JArray(List(first: JDouble, second: JDouble)) => (first.num, second.num)
    })
    val location = Location(points)
    Some(Stash(nameValue, location))
  }

  private def getPointLocation(nameValue: String, location: JValue): Option[Stash] = {
    val longitude = location \ "longitude"
    val latitude = location \ "latitude"
    if (longitude == JNothing || latitude == JNothing) {
      return None
    }
    Some(Stash(nameValue, Location(longitude.extract[String].toDouble, latitude.extract[String].toDouble)))
  }
}
