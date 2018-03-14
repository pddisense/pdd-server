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
import ucl.pdd.api.{Campaign, instantOrdering}
import ucl.pdd.storage.{CampaignQuery, CampaignStore}

import scala.collection.JavaConverters._

private[memory] final class MemoryCampaignStore extends CampaignStore {
  private[this] val index = new ConcurrentHashMap[String, Campaign]().asScala

  override def create(campaign: Campaign): Future[Boolean] = {
    Future.value(index.putIfAbsent(campaign.name, campaign).isEmpty)
  }

  override def replace(campaign: Campaign): Future[Boolean] = {
    Future.value(index.replace(campaign.name, campaign).isDefined)
  }

  override def list(query: CampaignQuery): Future[Seq[Campaign]] = {
    Future.value(index.values.filter(query.matches).toSeq.sortBy(_.createTime).reverse)
  }

  override def get(name: String): Future[Option[Campaign]] = Future.value(index.get(name))
}
