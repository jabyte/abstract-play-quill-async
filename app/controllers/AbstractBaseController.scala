package controllers

import dao.AbstractDAO
import models.AbstractModel
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.Future

class AbstractBaseController[T >: AbstractModel](dao: AbstractDAO[T]) extends Controller {

  implicit val writes: Writes[T] = Json.writes[T]

  def findAll: Action[AnyContent] = Action.async { request =>
    dao.findAll map {
      case () => BadRequest
      case list => Ok(Json.toJson(list))
    }
  }
  def create = Action.async(parse.json) { request =>
    Json.fromJson[T](request.body).fold(
      invalid => Future.successful(BadRequest),
      user => {
        dao.create(user).map(data =>
          Created.withHeaders(LOCATION -> s"/${dao.tableName}/${data.id}")
        )
      }
    )
  }

  def delete(id: Long) = Action.async { request =>
    dao.lookup(id) flatMap {
      case None => Future.successful(NotFound)
      case Some(user) =>
        dao.delete(user).map(_ => NoContent)
    }
  }

  def update(id: Long) = Action.async(parse.json) { request =>
    Json.fromJson[T](request.body).fold(
      invalid => Future.successful(BadRequest),
      data => {
        dao.update(data).map(_ => NoContent)
      }
    )
  }

}
