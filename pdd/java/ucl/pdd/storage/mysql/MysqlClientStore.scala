/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{OK, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.domain.Client
import ucl.pdd.storage.ClientStore
import ucl.pdd.storage.mysql.query.QueryBuilder

private[mysql] final class MysqlClientStore(mysql: MysqlClient)
  extends ClientStore with MysqlStore {

  import MysqlStore._

  private[this] val query = new QueryBuilder(mysql, "clients", hydrate)

  override def create(client: Client): Future[Boolean] = {
    query.insert
      .set("name", client.name)
      .set("createTime", client.createTime)
      .set("publicKey", client.publicKey)
      .set("browser", client.browser)
      .set("externalName", client.externalName)
      .execute()
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(client: Client): Future[Boolean] = {
    query.update
      .where("name = ?", client.name)
      .set("publicKey", client.publicKey)
      .set("browser", client.browser)
      .set("externalName", client.externalName)
      .execute()
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def delete(name: String): Future[Boolean] = {
    query.delete
      .where("name = ?", name)
      .execute()
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def list(): Future[Seq[Client]] = {
    query.select
      .orderBy("createTime", "desc")
      .execute()
  }

  override def count(): Future[Int] = {
    query.select.count()
  }

  override def get(name: String): Future[Option[Client]] = {
    query.select
      .where("name = ?", name).limit(1)
      .execute()
      .map(_.headOption)
  }

  override def multiGet(names: Seq[String]): Future[Seq[Option[Client]]] = {
    if (names.isEmpty) {
      Future.value(Seq.empty)
    } else {
      query.select
        .whereIn("name", names.map(wrapString): _*)
        .execute()
        .map(clients => names.map(name => clients.find(_.name == name)))
    }
  }

  private def hydrate(row: Row): Client = {
    Client(
      name = toString(row, "name"),
      createTime = toInstant(row, "createTime"),
      browser = toString(row, "browser"),
      publicKey = toString(row, "publicKey"),
      externalName = getString(row, "externalName"))
  }
}
