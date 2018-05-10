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

package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import ucl.pdd.api.Aggregation
import ucl.pdd.storage.AggregationStore

import scala.collection.JavaConverters._

private[memory] final class MemoryAggregationStore extends AggregationStore {
  private[this] val index = new ConcurrentHashMap[String, Aggregation]().asScala

  override def create(aggregation: Aggregation): Future[Boolean] = Future {
    index.putIfAbsent(aggregation.name, aggregation).isEmpty
  }

  override def replace(aggregation: Aggregation): Future[Boolean] = Future {
    index.replace(aggregation.name, aggregation).isDefined
  }

  override def list(query: AggregationStore.Query): Future[Seq[Aggregation]] = Future {
    index.values
      .filter(matches(query, _))
      .toSeq
      .sortWith { case (a, b) => a.day > b.day }
  }

  override def get(name: String): Future[Option[Aggregation]] = Future {
    index.get(name)
  }

  override def delete(query: AggregationStore.Query): Future[Unit] = Future {
    index
      .filter { case (_, v) => matches(query, v) }
      .keys
      .foreach(index.remove)
  }

  private def matches(query: AggregationStore.Query, aggregation: Aggregation): Boolean = {
    query.campaignName == aggregation.campaignName
  }
}
