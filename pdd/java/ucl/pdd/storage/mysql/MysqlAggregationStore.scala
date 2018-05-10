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
