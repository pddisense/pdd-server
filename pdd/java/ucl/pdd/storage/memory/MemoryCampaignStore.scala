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

package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentHashMap

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

  override def list(query: CampaignStore.Query): Future[Seq[Campaign]] = Future {
    index.values
      .filter(matches(query, _))
      .toSeq
      .sortWith { case (a, b) => a.createTime.compareTo(b.createTime) >= 0 }
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
