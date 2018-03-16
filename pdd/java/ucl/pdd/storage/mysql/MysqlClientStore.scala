/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{OK, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Client
import ucl.pdd.storage.ClientStore

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
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def list(query: ClientStore.Query): Future[Seq[Client]] = {
    val where = mutable.ListBuffer.empty[String]
    query.hasLeft.foreach {
      case true => where += "leaveTime is not null"
      case false => where += "leaveTime is null"
    }
    val sql = "select * " +
      "from clients " +
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
      name = toString(row, "name"),
      createTime = toInstant(row, "createTime"),
      browser = toString(row, "browser"),
      publicKey = toString(row, "publicKey"),
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
