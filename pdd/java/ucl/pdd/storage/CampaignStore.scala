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

package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Campaign

trait CampaignStore {
  def create(campaign: Campaign): Future[Boolean]

  def replace(campaign: Campaign): Future[Boolean]

  def list(query: CampaignStore.Query = CampaignStore.Query()): Future[Seq[Campaign]]

  def get(name: String): Future[Option[Campaign]]

  def batchGet(names: Seq[String]): Future[Seq[Option[Campaign]]] = Future.collect(names.map(get))
}

object CampaignStore {

  case class Query(isActive: Option[Boolean] = None)

}
