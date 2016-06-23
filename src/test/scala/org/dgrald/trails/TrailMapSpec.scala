package org.dgrald.trails

import org.scalatra.test.specs2._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class TrailMapSpec extends MutableScalatraSpec {

  implicit val jsonFormats = DefaultFormats

  val trailStoreStub = new TrailStoreStub
  addServlet(new TrailMap(trailStoreStub), "/*")

  val trail1Name = "Trail 1"
  val trail1 = Trail(trail1Name, new Location(22.55, 25.22))

  val trail2Name = "Trail 2"
  val trail2 = Trail(trail2Name, new Location(22.22, 23.33))
  val allTrails = List(trail1, trail2)

  val trail3Name = "Trail 3"
  val trail3 = Trail(trail3Name, new Location(33.33, 34.44))

  val newTrailJObject = ("name" -> trail3Name) ~ ("location" -> (("longitude" -> "22") ~ ("latitude" -> "33")))
  val newTrailJson = compact(render(newTrailJObject))

  "GET /trails/:id" should {
    "return status 200 and the trail that matches the input id" in {
      get("/trails/2") {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody \ "name" must_== JString(trail2.name)
        val location = jsonBody \ "location"
        location \ "longitude" must_== JDouble(trail2.location.longitude)
        location \ "latitude" must_== JDouble(trail2.location.latitude)
      }
    }

    "return a 404 when the specified id does not exist" in {
      get("/trails/idThatDoesNotExist") {
        status must_== 404
      }
    }
  }

  "GET /trails" should {
    "return status 200 and all of the trails when no id is specified" in {
      get("/trails") {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody(0) \ "name" must_== JString(trail1Name)
        jsonBody(1) \ "name" must_== JString(trail2Name)
      }
    }
  }

  "POST /trails" should {
    "return status 200 and the JSON of the new trail when sent a valid trail JSON" in {
      post("/trails", newTrailJson) {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody \ "name" must_== JString(trail3Name)
        val location = jsonBody \ "location"
        location \ "longitude" must_== JDouble(trail3.location.longitude)
        location \ "latitude" must_== JDouble(trail3.location.latitude)
      }
    }

    "return status 400 when the JSON of the new trail does not have a location" in {
      post("/trails", compact(render(newTrailJObject \ "name"))) {
        status must_== 400
      }
    }

    "return status 400 when the JSON of the new trail does not have a name" in {
      post("/trails", compact(render(newTrailJObject \ "location"))) {
        status must_== 400
      }
    }

    "return status 400 when the JSON of the new trail does not have a longitude" in {
      post("/trails", compact(render(("name" -> "some name") ~ ("location" -> ("latitude" -> "44"))))) {
        status must_== 400
      }
    }

    "return status 400 when the JSON of the new trail does not have a latitude" in {
      post("/trails", compact(render(("name" -> "some name") ~ ("location" -> ("longitude" -> "22"))))) {
        status must_== 400
      }
    }
  }

  class TrailStoreStub extends TrailStore {
    override def getTrails: Seq[Trail] = {
      allTrails
    }

    override def getTrail(id: String): Option[Trail] = id match {
      case "2" => Some(trail2)
      case _ => None
    }

    override def saveTrail(trail: Trail): Trail = trail3

    override def deleteTrail(trail: Trail): Unit = ???
  }
}
