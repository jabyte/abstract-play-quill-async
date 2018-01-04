package dao

import db.DbContext
import models.AbstractModel

case class User(
                 userID: Long,
                 name: String,
                 lastName: String,
                 email: String,
                 password: String,
                 birthdate: String
               ) extends AbstractModel(userID)

class UserDAO(val dbContext: DbContext) extends AbstractDAO[User](dbContext, "Users") {

}
