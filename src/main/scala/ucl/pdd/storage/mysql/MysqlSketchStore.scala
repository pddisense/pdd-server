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

import com.twitter.finagle.mysql.{OK, Parameter, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.domain.Sketch
import ucl.pdd.storage.SketchStore

import scala.collection.mutable

private[mysql] final class MysqlSketchStore(mysql: MysqlClient) extends SketchStore with MysqlStore {

  import MysqlStore._

  override def create(sketch: Sketch): Future[Boolean] = {
    val sql = "insert into sketches(name, createTime, clientName, campaignName, `group`, day, publicKey, " +
      "queriesCount, encryptedValues, rawValues, submitted) " +
      "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    mysql
      .prepare(sql)
      .apply(
        sketch.name,
        sketch.createTime,
        sketch.clientName,
        sketch.campaignName,
        sketch.group,
        sketch.day,
        sketch.publicKey,
        sketch.queriesCount,
        sketch.encryptedValues,
        sketch.rawValues,
        sketch.submitted)
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(sketch: Sketch): Future[Boolean] = {
    val sql = "update sketches " +
      "set createTime = ?, clientName = ?, campaignName = ?, `group` = ?, day = ?, publicKey = ?, " +
      "queriesCount = ?, encryptedValues = ?, rawValues = ?, submitted = ? " +
      "where name = ?"
    mysql
      .prepare(sql)
      .apply(
        sketch.createTime,
        sketch.clientName,
        sketch.campaignName,
        sketch.group,
        sketch.day,
        sketch.publicKey,
        sketch.queriesCount,
        sketch.encryptedValues,
        sketch.rawValues,
        sketch.submitted,
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
      where += "`group` = ?"
      params += group
    }
    query.submitted.foreach {
      case true => where += "submitted = 1"
      case false => where += "submitted = 0"
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
      createTime = toInstant(row, "createTime"),
      clientName = toString(row, "clientName"),
      campaignName = toString(row, "campaignName"),
      group = toInt(row, "group"),
      day = toInt(row, "day"),
      publicKey = toString(row, "publicKey"),
      queriesCount = toInt(row, "queriesCount"),
      encryptedValues = getStrings(row, "encryptedValues"),
      rawValues = getLongs(row, "rawValues"),
      submitted = toBoolean(row, "submitted"))
  }
}
