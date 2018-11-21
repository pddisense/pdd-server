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

private[mysql] final class MysqlClientStore(mysql: MysqlClient) extends ClientStore with MysqlStore {

  import MysqlStore._

  override def create(client: Client): Future[Boolean] = {
    val sql = "insert into clients(name, createTime, publicKey, browser, externalName) " +
      "values (?, ?, ?, ?, ?)"
    mysql
      .prepare(sql)
      .apply(
        client.name,
        client.createTime,
        client.publicKey,
        client.browser,
        client.externalName)
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(client: Client): Future[Boolean] = {
    val sql = "update clients " +
      "set createTime = ?, publicKey = ?, browser = ?, externalName = ? " +
      "where name = ?"
    mysql
      .prepare(sql)
      .apply(
        client.createTime,
        client.publicKey,
        client.browser,
        client.externalName,
        client.name)
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def delete(name: String): Future[Boolean] = {
    mysql
      .prepare("delete from clients where name = ?")
      .apply(name)
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def list(): Future[Seq[Client]] = {
    mysql
      .prepare("select * from clients order by createTime desc")
      .select()(hydrate)
  }

  override def count(): Future[Int] = {
    mysql
      .prepare("select count(1) c from clients")
      .select()(toLong(_, "c").toInt)
      .map(_.head)
  }

  override def get(name: String): Future[Option[Client]] = {
    mysql
      .prepare("select * from clients where name = ? limit 1")
      .select(name)(hydrate)
      .map(_.headOption)
  }

  override def multiGet(names: Seq[String]): Future[Seq[Option[Client]]] = {
    if (names.isEmpty) {
      Future.value(Seq.empty)
    } else {
      mysql
        .prepare(s"select * " +
          s"from clients " +
          s"where name = in (${Seq.fill(names.size)("?").mkString(",")})")
        .select(names.map(wrapString): _*)(hydrate)
        .map(campaigns => names.map(name => campaigns.find(_.name == name)))
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
