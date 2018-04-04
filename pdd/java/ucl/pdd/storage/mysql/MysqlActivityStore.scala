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
