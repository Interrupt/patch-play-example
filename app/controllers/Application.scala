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

    val jsonResult = patch.get.op match {
      case "put" => {
        val path = applyPathBits(__, startPath.toList)

        val transform = path.json.update(
          __.read[JsObject].map{ o => o ++ Json.obj( key -> patch.get.value ) }
        )

        obj.transform(transform) match {
          case o: JsSuccess[JsObject] => {
            Json.fromJson[Event](o.get) match {
              case s: JsSuccess[Event] => {
                testEvent = s.get // update the test event in memory to simulate persistence
                o.get
              }
              case _ => getJsonError("Could not save updated event!")
            }
          }
          case _ => getJsonError(s"Field '${patch.get.path}' does not exist!")
        }
      }
      case _ => { getJsonError(s"Operation '${patch.get.op}' not supported.") }
    }

    Ok(jsonResult)
  }

  def getJsonError(message: String): JsObject = Json.obj("error" -> message)

  def applyPathBits(path: JsPath, restOfPath: List[String]): JsPath = {
    if(restOfPath.size == 0) {
      path
    }
    else {
      applyPathBits(path \ restOfPath.head, restOfPath.drop(1))
    }
  }
}