package org.dgrald.trails

import java.util.UUID

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.specs2._
import org.specs2.mock.Mockito

class StashMapSpec extends MutableScalatraSpec with Mockito {

  implicit val jsonFormats = DefaultFormats

  val idOfTrailThatDoesNotExist = UUID.randomUUID().toString

  val trail1Name = "Trail 1"
  val trail1 = Stash(trail1Name, Location(22.55, 25.22))
  val trail1Json = getTrailJson(trail1)

  val trail2Name = "Trail 2"
  val trail2Id = "trail2Id"
  val trail2 = new Stash(trail2Id, trail2Name, Location(22.22, 23.33))
  val trail2Json = getTrailJson(trail2)

  val trail3Name = "Trail 3"
  val trail3Lat = 33.33
  val trail3Long = 34.44
  val trail3 = Stash(trail3Name, Location(trail3Lat, trail3Long))
  val trail3Json = getTrailJson(trail3)
  val trai3JsonString = compact(render(trail3Json))

  val allTrails = List(trail1, trail2, trail3)

  val trailStoreMock = mock[StashStore]
  val jsonConvererMock = mock[JsonConverter]

  private def getTrailJson(stash: Stash) = stash.location match {
    case point: PointLocation => ("name" -> stash.name) ~ ("location" -> ("longitude" -> point.longitude) ~ ("latitude" -> point.latitude))
  }

  private def setUpTrailStoreMock: Unit = {
    trailStoreMock.getTrail(trail2Id).returns(Some(trail2))
    trailStoreMock.getTrail(idOfTrailThatDoesNotExist).returns(None)
    trailStoreMock.getTrails.returns(allTrails)
    doReturn(trail3).when(trailStoreMock).saveTrail(any[Stash])
    doReturn(trail3).when(trailStoreMock).updateTrail(any[Stash])

    doReturn(trail1Json).when(jsonConvererMock).createTrailJson(trail1)
    doReturn(trail2Json).when(jsonConvererMock).createTrailJson(trail2)
    doReturn(trail3Json).when(jsonConvererMock).createTrailJson(trail3)
    doReturn(Some(trail3)).when(jsonConvererMock).getTrailFromJson(trai3JsonString)

  }

  setUpTrailStoreMock

  val servlet = new StashMap(trailStoreMock, jsonConvererMock)

  addServlet(servlet, "/*")

  "GET /trails/:id" should {
    "return status 200 and the trail that matches the input id" in {
      get("/trails/" + trail2Id) {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody must_== trail2Json
      }
    }

    "return a 404 when the specified id does not exist" in {
      get("/trails/" + idOfTrailThatDoesNotExist) {
        status must_== 404
        body must_== s"Could not find a trail with the ID $idOfTrailThatDoesNotExist"
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
        jsonBody(2) \ "name" must_== JString(trail3Name)
      }
    }
  }

  "POST /trails" should {

    "return status 200 and the JSON of the new trail when sent a valid trail JSON point location" in {
      post("/trails", compact(render(trail3Json))) {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody \ "name" must_== JString(trail3Name)
        val location = jsonBody \ "location"

        trail3.location  match {
          case point: PointLocation =>
            location \ "longitude" must_== JDouble(point.longitude)
            location \ "latitude" must_== JDouble(point.latitude)
        }
      }
    }

    "return status 400 when given improper json" in {
      val json = compact(render(trail3Json \ "name"))
      doReturn(None).when(jsonConvererMock).getTrailFromJson(json)

      put("/trails", json) {
        status must_== 400
      }
    }
  }

  "PUT /trails" should {

    "return status 200 and the JSON of the new trail when sent a valid trail JSON" in {
      put("/trails", trai3JsonString) {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody \ "name" must_== JString(trail3Name)
        val location = jsonBody \ "location"
        trail3.location  match {
          case point: PointLocation =>
            location \ "longitude" must_== JDouble(point.longitude)
            location \ "latitude" must_== JDouble(point.latitude)
        }
      }
    }

    "return status 400 when given improper json" in {
      val json = compact(render(trail3Json \ "name"))
      doReturn(None).when(jsonConvererMock).getTrailFromJson(json)

      put("/trails", json) {
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
        body must_== s"Could not find a trail with the ID $idOfTrailThatDoesNotExist"
      }
    }
  }
}
