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

import com.github.nscala_time.time.Imports._
import com.twitter.util.Future
import ucl.pdd.api.Campaign
import ucl.pdd.storage.CampaignStore

import scala.collection.JavaConverters._

private[memory] final class MemoryCampaignStore extends CampaignStore {
  private[this] val index = new ConcurrentHashMap[String, Campaign]().asScala

  override def create(campaign: Campaign): Future[Boolean] = Future {
    index.putIfAbsent(campaign.name, campaign).isEmpty
  }

  override def replace(campaign: Campaign): Future[Boolean] = Future {
    index.replace(campaign.name, campaign).isDefined
  }

  override def delete(name: String): Future[Unit] = Future {
    index.remove(name)
  }

  override def list(query: CampaignStore.Query): Future[Seq[Campaign]] = Future {
    index.values
      .filter(matches(query, _))
      .toSeq
      .sortWith { case (a, b) => a.createTime > b.createTime }
  }

  override def count(query: CampaignStore.Query): Future[Int] = Future {
    index.values.count(matches(query, _))
  }

  override def get(name: String): Future[Option[Campaign]] = Future {
    index.get(name)
  }

  override def batchGet(names: Seq[String]): Future[Seq[Option[Campaign]]] = {
    Future.value(names.map(index.get))
  }

  private def matches(query: CampaignStore.Query, campaign: Campaign): Boolean = {
    query.isActive.forall(campaign.isActive == _)
  }
}
