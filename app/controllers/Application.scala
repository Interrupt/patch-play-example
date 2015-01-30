package controllers

import play.api.libs.json._
import play.api.mvc._

object Application extends Controller {

  implicit val artistWriter = Json.writes[Artist]
  implicit val artistReader = Json.reads[Artist]
  implicit val eventWriter = Json.writes[Event]
  implicit val eventReader = Json.reads[Event]

  implicit val patchReader = Json.reads[Patch]

  case class Patch(op: String, path: String, value: String)

  case class Event(id: String, name: String, date: String, headliner: Artist )
  case class Artist(id: String, name: String)

  var testEvent = Event("145", "My First Event", "2/4/15", Artist("1234", "Fuzz and the Guns"))

  def listEvents = Action {
    Ok(Json.toJson(testEvent))
  }

  def patchEvent = Action(BodyParsers.parse.json) { request =>
    val patch = Json.fromJson[Patch](request.body)

    val obj: JsObject = Json.toJson(testEvent).as[JsObject]

    val pathList = patch.get.path.split("/").toList
    val key = pathList.takeRight(1).head
    val startPath = pathList.dropRight(1)

    testEvent = patch.get.op match {
      case "put" => {
        val path = applyPathBits(__, startPath.toList)

        val transform = path.json.update(
          __.read[JsObject].map{ o => o ++ Json.obj( key -> patch.get.value ) }
        )

        Json.fromJson[Event](obj.transform(transform).get).get
      }
      case _ => { testEvent }
    }

    Ok(Json.toJson(testEvent))
  }

  def applyPathBits(path: JsPath, restOfPath: List[String]): JsPath = {
    if(restOfPath.size == 0) {
      path
    }
    else {
      applyPathBits(path \ restOfPath.head, restOfPath.drop(1))
    }
  }
}