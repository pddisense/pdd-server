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

import com.twitter.finagle.mysql.{OK, Parameter, Row, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Activity
import ucl.pdd.storage.ActivityStore

import scala.collection.mutable

private[mysql] final class MysqlActivityStore(mysql: MysqlClient) extends ActivityStore with MysqlStore {

  import MysqlStore._

  override def create(activity: Activity): Future[Unit] = {
    val sql = "insert into activity(clientName, time, countryCode) values (?, ?, ?)"
    mysql
      .prepare(sql)
      .apply(activity.clientName, activity.time, activity.countryCode)
      .unit
  }

  override def list(query: ActivityStore.Query): Future[Seq[Activity]] = {
    val where = mutable.ListBuffer.empty[String]
    val params = mutable.ListBuffer.empty[Parameter]
    query.countryCode.foreach { countryCode =>
      where += "countryCode = ?"
      params += countryCode
    }
    query.clientName.foreach { clientName =>
      where += "clientName = ?"
      params += clientName
    }
    query.startTime.foreach { startTime =>
      where += "time >= ?"
      params += startTime
    }
    query.endTime.foreach { endTime =>
      where += "time <= ?"
      params += endTime
    }

    val sql = "select * " +
      "from activity " +
      s"where ${if (where.isEmpty) "true" else where.mkString(" and ")}"
    mysql
      .prepare(sql)
      .select(params: _*)(hydrate)
  }

  override def delete(query: ActivityStore.Query): Future[Int] = {
    val where = mutable.ListBuffer.empty[String]
    val params = mutable.ListBuffer.empty[Parameter]
    query.countryCode.foreach { countryCode =>
      where += "countryCode = ?"
      params += countryCode
    }
    query.clientName.foreach { clientName =>
      where += "clientName = ?"
      params += clientName
    }
    query.startTime.foreach { startTime =>
      where += "time >= ?"
      params += startTime
    }
    query.endTime.foreach { endTime =>
      where += "time <= ?"
      params += endTime
    }

    val sql = "delete " +
      "from activity " +
      s"where ${if (where.isEmpty) "true" else where.mkString(" and ")}"
    mysql
      .prepare(sql)
      .apply(params: _*)
      .map {
        case ok: OK => ok.affectedRows.toInt
        case _ => 0
      }
  }

  private def hydrate(row: Row): Activity = {
    Activity(
      clientName = toString(row, "clientName"),
      time = toInstant(row, "time"),
      countryCode = getString(row, "countryCode"))
  }
}
