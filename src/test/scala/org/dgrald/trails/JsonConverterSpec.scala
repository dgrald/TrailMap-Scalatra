package org.dgrald.trails

import org.specs2.mutable.Specification
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.specification.core.Fragment

/**
  * Created by dylangrald on 10/19/16.
  */
class JsonConverterSpec extends Specification {

  implicit val jsonFormats = DefaultFormats

  val jsonConverter = JsonConverter()

  val pointStashName = AnyRandom.string()
  val pointLongitude = 22
  val pointLatitude = 44
  val pointLocation = Location(longitude = pointLongitude, latitude = pointLatitude)
  val pointStash = Stash(pointStashName, pointLocation)
  val pointStashJson = parse(s"""{"name": "$pointStashName", "location": {"type": "Point", "coordinates": [$pointLongitude, $pointLatitude]}}"""")

  val lineStashName = AnyRandom.string()
  val lineStashPoints = List((1.1, 1.1), (2.2, 2.2), (3.3, 3.3))
  val lineStashLocation = Location(lineStashPoints)
  val lineStash = Stash(lineStashName, lineStashLocation)
  val lineStashPointJsonArray = lineStashPoints.map(e => e match {case (first, second) => s"[$first, $second]"}).mkString(", ")
  val lineStashJson = parse(s"""{"name": "$lineStashName", "location": {"type": "LineString", "coordinates": [$lineStashPointJsonArray]}}""")

  val polygonStashName = AnyRandom.string()
  val polygonStashPoints = List((1.1, 1.1), (2.2, 2.2), (3.3, 3.3))
  val polygonStashLocation = new PolygonLocation(polygonStashPoints)
  val polygonStash = Stash(polygonStashName, polygonStashLocation)
  val polygonStashPointJsonArray = polygonStashPoints.map(e => e match {case (first, second) => s"[$first, $second]"}).mkString(", ")
  val polygonStashJson = parse(s"""{"name": "$polygonStashName", "location": {"type": "Polygon", "coordinates": [$polygonStashPointJsonArray]}}""")

  "Should convert a stash to JSON given a point location stash" in {
    val json = jsonConverter.createTrailJson(pointStash)

    (json \ "name").extract[String] must_== pointStashName
    val jsonLocation = json \ "location"
    (jsonLocation \ "type").extract[String] must_== "Point"
    (jsonLocation \ "coordinates").extract[List[Double]] must_== List(pointLongitude, pointLatitude)
  }

  "Should convert a stash to JSON given a line location stash" in {
    val json = jsonConverter.createTrailJson(lineStash)

    (json \ "name").extract[String] must_== lineStashName
    val jsonLocation = json \ "location"
    (jsonLocation \ "type").extract[String] must_== "LineString"
    val points = (jsonLocation \ "coordinates").extract[List[List[Double]]]
    points.foreach(d => d.length must_== 2)
    val tuples = points.map(d => (d.head, d.last))
    tuples must_== lineStashPoints
  }


  "Should convert a stash to JSON given a line location stash" in {
    val json = jsonConverter.createTrailJson(polygonStash)

    (json \ "name").extract[String] must_== polygonStashName
    val jsonLocation = json \ "location"
    (jsonLocation \ "type").extract[String] must_== "Polygon"
    val points = (jsonLocation \ "coordinates").extract[List[List[Double]]]
    points.foreach(d => d.length must_== 2)
    points.map(d => (d.head, d.last)) must_== polygonStashPoints
  }

  "Should return a stash from properly formatted line stash JSON" in {
    val convertedLineStash = jsonConverter.getTrailFromJson(compact(render(lineStashJson))).get

    convertedLineStash.name must_== lineStashName
    convertedLineStash.location match {
      case line: LineLocation =>
        line.linePoints must_== lineStashPoints
    }
  }

  "Should return a stash from properly formatted point stash JSON" in {
    val convertedPointStash = jsonConverter.getTrailFromJson(compact(render(pointStashJson))).get

    convertedPointStash.name must_== pointStashName
    convertedPointStash.location match {
      case point: PointLocation =>
        point.longitude must_== pointLongitude
        point.latitude must_== pointLatitude
    }
  }

  "Should return a stash from properly formatted polygon stash JSON" in {
    val convertedPolygonStash = jsonConverter.getTrailFromJson(compact(render(polygonStashJson))).get

    convertedPolygonStash.name must_== polygonStashName
    convertedPolygonStash.location match {
      case polygonStash: PolygonLocation =>
        polygonStash.polygonPoints must_== polygonStashPoints
    }
  }

  "Should return None when given" in {
    val noName = compact(render(pointStashJson \ "location"))
    val noLocation = compact(render(pointStashJson \ "name"))
    val invalidCoordinates = compact(render(parse(s"""{"name": "$pointStashName", "location": {"type": "Point", "coordinates": [$pointLongitude]}}"""")))

    Fragment.foreach(List(("no name", noName), ("no location", noLocation), ("invalid coordinates", invalidCoordinates))) {
      case (testCaseScenario: String, json: String) =>
        s"$testCaseScenario" ! {
          jsonConverter.getTrailFromJson(json) must_== None
        }
    }
  }
}
