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

import com.google.common.util.concurrent.AbstractIdleService
import com.twitter.finagle.mysql.{ServerError, Client => MysqlClient}
import com.twitter.util.{Await, Future}
import ucl.pdd.storage._

final class MysqlStorage(mysql: MysqlClient) extends AbstractIdleService with Storage {
  override val clients = new MysqlClientStore(mysql)

  override val campaigns = new MysqlCampaignStore(mysql)

  override val aggregations = new MysqlAggregationStore(mysql)

  override val sketches = new MysqlSketchStore(mysql)

  override def startUp(): Unit = {
    val tables = MysqlClientStore.CreateSchemaDDL
    val fs = tables.map { case (tableName, ddl) =>
      mysql
        .query(s"select 1 from `$tableName` limit 1")
        .rescue {
          // Error code 1146 corresponds to a table that does not exist.
          case ServerError(1146, _, _) =>
            //logger.info("Creating MySQL storage schema")
            mysql.query(ddl).unit
        }
    }.toSeq
    Await.result(Future.join(fs))
  }

  override def shutDown(): Unit = {
    Await.ready(mysql.close())
  }
}
