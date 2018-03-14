/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{OK, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api.Client
import ucl.pdd.storage.{ClientQuery, ClientStore}

import scala.collection.mutable

private[mysql] final class MysqlClientStore(mysql: MysqlClient) extends ClientStore with MysqlStore {

  import MysqlStore._

  override def create(client: Client): Future[Boolean] = {
    val sql = "insert into clients(name, createTime, publicKey, browser, externalName, leaveTime) " +
      "values (?, ?, ?, ?, ?, ?)"
    mysql
      .prepare(sql)
      .apply(
        client.name,
        client.createTime,
        client.publicKey,
        client.browser,
        client.externalName,
        client.leaveTime)
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(client: Client): Future[Boolean] = {
    val sql = "update clients " +
      "set createTime = ?, publicKey = ?, browser = ?, externalName = ?, leaveTime = ? " +
      "where name = ?"
    mysql
      .prepare(sql)
      .apply(
        client.createTime,
        client.publicKey,
        client.browser,
        client.externalName,
        client.leaveTime,
        client.name)
      .map { case ok: OK => ok.affectedRows == 1 }
  }

  override def list(query: ClientQuery): Future[Seq[Client]] = {
    val where = mutable.ListBuffer.empty[String]
    query.hasLeft.foreach {
      case true => where += "leaveTime is not null"
      case false => where += "leaveTime is null"
    }
    val sql = "select * from clients " +
      s"where ${if (where.isEmpty) "true" else where.mkString(" and ")} " +
      "order by createTime desc"
    mysql
      .prepare(sql)
      .select()(hydrate)
  }

  override def get(name: String): Future[Option[Client]] = {
    mysql
      .prepare("select * from clients where name = ? limit 1")
      .select(name)(hydrate)
      .map(_.headOption)
  }

  private def hydrate(row: Row): Client = {
    Client(
      name = getString(row, "name").getOrElse(""),
      createTime = getInstant(row, "createTime").getOrElse(Instant.now()),
      browser = getString(row, "browser").getOrElse(""),
      publicKey = getString(row, "publicKey").getOrElse(""),
      externalName = getString(row, "externalName"),
      leaveTime = getInstant(row, "leaveTime"))
  }
}

private[mysql] object MysqlClientStore {
  val CreateSchemaDDL = Map(
    "clients" -> ("create table clients(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "createTime timestamp not null," +
      "publicKey varchar(255) not null," +
      "browser varchar(255) not null," +
      "externalName varchar(255) not null," +
      "leaveTime timestamp null," +
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8"))
}
