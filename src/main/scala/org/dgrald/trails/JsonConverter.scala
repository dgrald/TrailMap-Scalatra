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

    val locationType = (location \ "type").extract[String]

    locationType match {
      case "Point" => getPointLocation(nameValue, location)
      case _ => getPolygonOrLineLocation(nameValue, location, locationType)
    }
  }

  override def createTrailJson(trail: Stash): JsonAST.JObject = {
    def createLineOrPolygon(geometryType: String, inputPoints: List[(Double, Double)]) = {
      val points = inputPoints.map(p => p match {case (first: Double, second: Double) => JArray(List(JDouble(first), JDouble(second)))})
      "location" -> ("type" -> geometryType) ~ ("coordinates" -> JArray(points))
    }

    def createLocationJson(trail: Stash) = {
      trail.location match {
        case point: PointLocation => "location" -> ("type" -> "Point") ~ ("coordinates" -> JArray(List(JDouble(point.longitude), JDouble(point.latitude))))
        case line: LineLocation => createLineOrPolygon("LineString", line.linePoints)
        case polygon: PolygonLocation => createLineOrPolygon("Polygon", polygon.polygonPoints)
      }
    }

    ("id" -> trail.id) ~ ("name" -> trail.name) ~ createLocationJson(trail)
  }

  private def getPolygonOrLineLocation(nameValue: String, inputJson: JValue, geometryType: String): Some[Stash] = inputJson \ "coordinates" match {
    case JArray(coordinates) =>
      val points: List[(Double, Double)] = coordinates.map(s => s match {
        case JArray(List(first: JDouble, second: JDouble)) => (first.num, second.num)
      })
      val location = if(geometryType == "LineString") {
        Location(points)
      } else {
        new PolygonLocation(points)
      }
      Some(Stash(nameValue, location))
  }

  private def getPointLocation(nameValue: String, location: JValue): Option[Stash] = location \ "coordinates" match {
    case JArray(coordinates) => coordinates match {
      case List(longitude, latitude) => Some(Stash(nameValue, Location(longitude.extract[String].toDouble, latitude.extract[String].toDouble)))
      case _ => None
    }
  }
}
