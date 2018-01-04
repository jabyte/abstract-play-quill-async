package controllers

import dao.AbstractDAO
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, Controller}
import models.{AbstractModel, User, Users}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UsersController[T >: AbstractModel] (userServices: Users, dao: AbstractDAO[T]) extends Controller
{
    implicit val userWrites: Writes[User] = Json.writes[User]
    implicit val userReads: Reads[User] = (
        Reads.pure(0L) and
            (JsPath \ "name").read[String] and
            (JsPath \ "isActive").read[Boolean]
        ) (User.apply _)

    def get(id: Long) = Action.async { request =>
        userServices.find(id) map {
            case None => NotFound
            case Some(user) => Ok(Json.toJson(user))
        }
    }

    def findAll = Action.async { request =>
      dao.findAll map {
          case () => BadRequest("No content")
          case list => Ok(Json.toJson(list))
      }
    }

    def create = Action.async(parse.json) { request =>
        Json.fromJson[User](request.body).fold(
            invalid => Future.successful(BadRequest),
            user => {
                userServices.create(user).map(userCreated =>
                    Created.withHeaders(LOCATION -> s"/users/${userCreated.id}")
                )
            }
        )
    }

    def delete(id: Long) = Action.async { request =>
        userServices.find(id) flatMap {
            case None => Future.successful(NotFound)
            case Some(user) =>
                userServices.delete(user).map(_ => NoContent)
        }
    }

    def update(id: Long) = Action.async(parse.json) { request =>
        Json.fromJson[User](request.body).fold(
            invalid => Future.successful(BadRequest),
            user => {
                userServices.update(user.copy(id = id)).map(_ => NoContent)
            }
        )
    }
}
