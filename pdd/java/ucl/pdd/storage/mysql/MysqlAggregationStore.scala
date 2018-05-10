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
import ucl.pdd.api.{Aggregation, AggregationStats}
import ucl.pdd.storage.AggregationStore

private[mysql] final class MysqlAggregationStore(mysql: MysqlClient) extends AggregationStore with MysqlStore {

  import MysqlStore._

  override def create(aggregation: Aggregation): Future[Boolean] = {
    mysql
      .prepare("insert into aggregations(name, campaignName, day, decryptedValues, rawValues, " +
        "activeCount, submittedCount, decryptedCount) " +
        "values (?, ?, ?, ?, ?, ?, ?, ?)")
      .apply(
        aggregation.name,
        aggregation.campaignName,
        aggregation.day,
        aggregation.decryptedValues,
        aggregation.rawValues,
        aggregation.stats.activeCount,
        aggregation.stats.submittedCount,
        aggregation.stats.decryptedCount)
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(aggregation: Aggregation): Future[Boolean] = {
    mysql
      .prepare("update aggregations set campaignName = ?, day = ?, decryptedValues = ?, " +
        "rawValues = ?, activeCount = ?, submittedCount = ?, decryptedCount = ? " +
        "where name = ?")
      .apply(
        aggregation.campaignName,
        aggregation.day,
        aggregation.decryptedValues,
        aggregation.rawValues,
        aggregation.stats.activeCount,
        aggregation.stats.submittedCount,
        aggregation.stats.decryptedCount,
        aggregation.name)
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  override def get(name: String): Future[Option[Aggregation]] = {
    mysql
      .prepare("select * from aggregations where name = ? limit 1")
      .select(name)(hydrate)
      .map(_.headOption)
  }

  override def list(query: AggregationStore.Query): Future[Seq[Aggregation]] = {
    mysql
      .prepare("select * from aggregations where campaignName = ? order by day desc")
      .select(query.campaignName)(hydrate)
  }

  override def delete(query: AggregationStore.Query): Future[Unit] = {
    mysql
      .prepare("delete from aggregations where campaignName = ?")
      .apply(query.campaignName)
      .unit
  }

  private def hydrate(row: Row): Aggregation = {
    Aggregation(
      name = toString(row, "name"),
      campaignName = toString(row, "campaignName"),
      day = toInt(row, "day"),
      decryptedValues = toLongs(row, "decryptedValues"),
      rawValues = toLongs(row, "rawValues"),
      stats = AggregationStats(
        activeCount = toLong(row, "activeCount"),
        submittedCount = toLong(row, "submittedCount"),
        decryptedCount = toLong(row, "decryptedCount")))
  }
}
