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

  val trail2Name = "Trail 2"
  val trail2Id = "trail2Id"
  val trail2 = new Stash(trail2Id, trail2Name, Location(22.22, 23.33))

  val trail3Name = "Trail 3"
  val trail3 = Stash(trail3Name, Location(33.33, 34.44))

  val allTrails = List(trail1, trail2)

  val trailStoreMock = mock[StashStore]

  private def setUpTrailStoreMock: Unit = {
    trailStoreMock.getTrail(trail2Id).returns(Some(trail2))
    trailStoreMock.getTrail(idOfTrailThatDoesNotExist).returns(None)
    trailStoreMock.getTrails.returns(allTrails)
    doReturn(trail3).when(trailStoreMock).saveTrail(any[Stash])
    doReturn(trail3).when(trailStoreMock).updateTrail(any[Stash])
  }

  setUpTrailStoreMock

  val servlet = new StashMap(trailStoreMock)

  addServlet(servlet, "/*")

  "GET /trails/:id" should {
    "return status 200 and the trail that matches the input id" in {
      get("/trails/" + trail2Id) {
        status must_== 200
        val jsonBody = parse(body)
        jsonBody \ "name" must_== JString(trail2.name)
        jsonBody \ "id" must_== JString(trail2Id)
        val location = jsonBody \ "location"
        trail2.location  match {
          case point: PointLocation =>
            location \ "longitude" must_== JDouble(point.longitude)
            location \ "latitude" must_== JDouble(point.latitude)
        }
      }
    }

    "return a 404 when the specified id does not exist" in {
      get("/trails/" + idOfTrailThatDoesNotExist) {
        status must_== 404
        body must_== s"Could not find a trail with the ID ${idOfTrailThatDoesNotExist}"
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

        trail3.location  match {
          case point: PointLocation =>
            location \ "longitude" must_== JDouble(point.longitude)
            location \ "latitude" must_== JDouble(point.latitude)
        }
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

  "PUT /trails" should {
    val newTrailJObject = ("name" -> trail3Name) ~ ("location" -> (("longitude" -> "22") ~ ("latitude" -> "33")))
    val newTrailJson = compact(render(newTrailJObject))

    "return status 200 and the JSON of the new trail when sent a valid trail JSON" in {
      put("/trails", newTrailJson) {
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

    "return status 400 when the JSON of the new trail does not have a location" in {
      put("/trails", compact(render(newTrailJObject \ "name"))) {
        status must_== 400
      }
    }

    "return status 400 when the JSON of the new trail does not have a name" in {
      put("/trails", compact(render(newTrailJObject \ "location"))) {
        status must_== 400
      }
    }

    "return status 400 when the JSON of the new trail does not have a longitude" in {
      put("/trails", compact(render(("name" -> "some name") ~ ("location" -> ("latitude" -> "44"))))) {
        status must_== 400
      }
    }

    "return status 400 when the JSON of the new trail does not have a latitude" in {
      put("/trails", compact(render(("name" -> "some name") ~ ("location" -> ("longitude" -> "22"))))) {
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
