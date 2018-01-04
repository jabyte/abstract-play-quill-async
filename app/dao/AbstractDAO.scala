package dao

import db.DbContext
import models.{AbstractModel}

import scala.concurrent.Future

abstract class AbstractDAO[T <: AbstractModel](val db: DbContext, val tableName: String) {
  import db._
  val t: db.Quoted[db.EntityQuery[T]] = quote(querySchema[T](tableName))

  def lookup(id: Long): Future[Option[T]] = run(t
    .filter(c => c.id == lift(id)))
    .map(_.headOption)

  def create(data: T): Future[AbstractModel] =
    run(t.insert(lift(data)).returning(_.id)).map(newId => data.copy(ID = newId ))

  def delete(data: T): Future[Long] = run(t.filter(_.id == lift(data.id)).delete)

  def update(data: T): Future[Long] = run(t.filter(_.id == lift(data.id)).update(lift(data)))

  def findAll: Future[List[T]] = run(t)

  def findWithQuery(query: T => Boolean): Future[List[T]] = run(t.filter(query))
}
