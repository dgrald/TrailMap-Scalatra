package org.dgrald.trails

import java.util.UUID

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.specs2._
import org.specs2.mock.Mockito

class TrailMapSpec extends MutableScalatraSpec with Mockito {

  implicit val jsonFormats = DefaultFormats

  val idOfTrailThatDoesNotExist = UUID.randomUUID().toString

  val trail1Name = "Trail 1"
  val trail1 = Trail(trail1Name, new Location(22.55, 25.22))

  val trail2Name = "Trail 2"
  val trail2Id = "trail2Id"
  val trail2 = new Trail(trail2Id, trail2Name, new Location(22.22, 23.33))

  val trail3Name = "Trail 3"
  val trail3 = Trail(trail3Name, new Location(33.33, 34.44))

  val allTrails = List(trail1, trail2)

  val trailStoreMock = mock[TrailStore]
  trailStoreMock.getTrail(trail2Id).returns(Some(trail2))
  trailStoreMock.getTrail(idOfTrailThatDoesNotExist).returns(None)
  trailStoreMock.getTrails.returns(allTrails)
  doReturn(trail3).when(trailStoreMock).saveTrail(any[Trail])

  val servlet = new TrailMap(trailStoreMock)

  addServlet(servlet, "/*")

  "GET /trails/:id" should {
    "return status 200 and the trail that matches the input id" in {
      get("/trails/" + trail2Id) {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody \ "name" must_== JString(trail2.name)
        jsonBody \ "id" must_== JString(trail2Id)
        val location = jsonBody \ "location"
        location \ "longitude" must_== JDouble(trail2.location.longitude)
        location \ "latitude" must_== JDouble(trail2.location.latitude)
      }
    }

    "return a 404 when the specified id does not exist" in {
      get("/trails/" + idOfTrailThatDoesNotExist) {
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
    val newTrailJObject = ("name" -> trail3Name) ~ ("location" -> (("longitude" -> "22") ~ ("latitude" -> "33")))
    val newTrailJson = compact(render(newTrailJObject))

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

  "DELETE trails/:id" should {
    "delete the specified trail" in  {
      delete("/trails/" + trail2Id) {
        status must_== 204
        there was one(trailStoreMock).deleteTrail(trail2)
      }
    }

    "return a 404 if the specified trail is not present" in {
      delete("/trails/" + idOfTrailThatDoesNotExist) {
        status must_== 404
      }
    }
  }
}
