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

import com.twitter.finagle.mysql.{OK, Parameter, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Sketch
import ucl.pdd.storage.SketchStore

import scala.collection.mutable

private[mysql] final class MysqlSketchStore(mysql: MysqlClient) extends SketchStore with MysqlStore {

  import MysqlStore._

  override def create(sketch: Sketch): Future[Boolean] = {
    val sql = "insert into sketches(name, clientName, campaignName, `group`, day, publicKey, " +
      "encryptedValues, rawValues, submitTime) " +
      "values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
    mysql
      .prepare(sql)
      .apply(
        sketch.name,
        sketch.clientName,
        sketch.campaignName,
        sketch.group,
        sketch.day,
        sketch.publicKey,
        sketch.encryptedValues,
        sketch.rawValues,
        sketch.submitTime)
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(sketch: Sketch): Future[Boolean] = {
    val sql = "update sketches " +
      "set clientName = ?, campaignName = ?, `group` = ?, day = ?, publicKey = ?, " +
      "encryptedValues = ?, rawValues = ?, submitTime = ? " +
      "where name = ?"
    mysql
      .prepare(sql)
      .apply(
        sketch.clientName,
        sketch.campaignName,
        sketch.group,
        sketch.day,
        sketch.publicKey,
        sketch.encryptedValues,
        sketch.rawValues,
        sketch.submitTime,
        sketch.name)
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def delete(name: String): Future[Boolean] = {
    mysql
      .prepare("delete from sketches where name = ?")
      .apply(name)
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def list(query: SketchStore.Query): Future[Seq[Sketch]] = {
    val where = mutable.ListBuffer.empty[String]
    val params = mutable.ListBuffer.empty[Parameter]
    query.campaignName.foreach { campaignName =>
      where += "campaignName = ?"
      params += campaignName
    }
    query.clientName.foreach { clientName =>
      where += "clientName = ?"
      params += clientName
    }
    query.day.foreach { day =>
      where += "day = ?"
      params += day
    }
    query.group.foreach { group =>
      where += "group = ?"
      params += group
    }
    query.isSubmitted.foreach {
      case true => where += "submitTime is not null"
      case false => where += "submitTime is null"
    }

    val sql = "select * " +
      "from sketches " +
      s"where ${if (where.isEmpty) "true" else where.mkString(" and ")}"
    mysql
      .prepare(sql)
      .select(params: _*)(hydrate)
  }

  override def get(name: String): Future[Option[Sketch]] = {
    mysql
      .prepare("select * from sketches where name = ? limit 1")
      .select(name)(hydrate)
      .map(_.headOption)
  }

  private def hydrate(row: Row): Sketch = {
    Sketch(
      name = toString(row, "name"),
      clientName = toString(row, "clientName"),
      campaignName = toString(row, "campaignName"),
      group = toInt(row, "group"),
      day = toInt(row, "day"),
      publicKey = toString(row, "publicKey"),
      encryptedValues = getStrings(row, "encryptedValues"),
      rawValues = getLongs(row, "rawValues"),
      submitTime = getInstant(row, "submitTime"))
  }
}

private[mysql] object MysqlSketchStore {
  val CreateSchemaDDL = Map(
    "sketches" -> ("create table sketches(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "clientName varchar(255) not null," +
      "campaignName varchar(255) not null," +
      "`group` int not null," +
      "day int not null," +
      "publicKey varchar(255) not null," +
      "encryptedValues text not null," +
      "rawValues text not null," +
      "submitTime timestamp null," +
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8"))
}
