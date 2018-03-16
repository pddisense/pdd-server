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

import com.twitter.finagle.mysql.{ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.storage._

final class MysqlStorage(mysql: MysqlClient) extends Storage {
  override val clients = new MysqlClientStore(mysql)

  override val campaigns = new MysqlCampaignStore(mysql)

  override val aggregations = new MysqlAggregationStore(mysql)

  override val sketches = new MysqlSketchStore(mysql)

  override def startUp(): Future[Unit] = {
    val tables = MysqlClientStore.CreateSchemaDDL ++ MysqlCampaignStore.CreateSchemaDDL ++
      MysqlAggregationStore.CreateSchemaDDL ++ MysqlSketchStore.CreateSchemaDDL
    val fs = tables.map { case (tableName, ddl) =>
      mysql
        .query(s"select 1 from `$tableName` limit 1")
        .rescue {
          // Error code 1146 corresponds to a table that does not exist.
          case ServerError(1146, _, _) => mysql.query(ddl).unit
        }
    }.toSeq
    Future.join(fs)
  }

  override def shutDown(): Future[Unit] = {
    mysql.close()
  }
}
