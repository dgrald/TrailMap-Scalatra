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
  val pointStashJson = jsonConverter.createTrailJson(pointStash)

  val lineStashName = AnyRandom.string()
  val lineStashPoints = List((1.1, 1.1), (2.2, 2.2), (3.3, 3.3))
  val lineStashLocation = Location(lineStashPoints)
  val lineStash = Stash(lineStashName, lineStashLocation)
  val lineStashJson = jsonConverter.createTrailJson(lineStash)

  "Should convert a stash to JSON given a point location stash" in {
    val json = jsonConverter.createTrailJson(pointStash)

    (json \ "name").extract[String] must_== pointStashName
    val jsonLocation = json \ "location"
    (jsonLocation \ "longitude").extract[Double] must_== pointLongitude
    (jsonLocation \ "latitude").extract[Double] must_== pointLatitude
  }

  "Should convert a stash to JSON given a line location stash" in {
    val json = jsonConverter.createTrailJson(lineStash)

    (json \ "name").extract[String] must_== lineStashName
    val jsonLocation = json \ "location"
    val points = jsonLocation.extract[List[List[Double]]]
    points.foreach(d => d.length must_== 2)
    val tuples = points.map(d => (d.head, d.last))
    tuples must_== lineStashPoints
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

  "Should return None when given no" in {
    val noName = compact(render(pointStashJson \ "location"))
    val noLocation = compact(render(pointStashJson \ "name"))
    val noLongitude = compact(render(("name" -> AnyRandom.string()) ~ ("location" -> ("latitude" -> 22))))
    val noLatitude = compact(render(("name" -> AnyRandom.string()) ~ ("location" -> ("longitude" -> 33))))

    Fragment.foreach(List(("name", noName), ("location", noLocation), ("longitude", noLongitude), ("latitude", noLatitude))) {
      case (testCaseScenario: String, json: String) =>
        s"$testCaseScenario" ! {
          jsonConverter.getTrailFromJson(json) must_== None
        }
    }
  }
}
