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
  /**
   * Persist a new campaign, if no other campaign with the same name exists.
   *
   * @param campaign A campaign to create.
   * @return Whether the campaign was successfully created.
   */
  def create(campaign: Campaign): Future[Boolean]

  /**
   * Replace an existing campaign with a new one, if such an campaign with the same name
   * already exists. All fields will be modified according to the values of the new campaign.
   *
   * @param campaign A campaign to update.
   * @return Whether the campaign was successfully replaced.
   */
  def replace(campaign: Campaign): Future[Boolean]

  /**
   * Retrieve a single campaign by its name, if it exists.
   *
   * @param name A campaign name.
   */
  def get(name: String): Future[Option[Campaign]]

  /**
   * Retrieve several campaigns by their names, if they exist.
   *
   * @param names A list of campaign names.
   */
  def batchGet(names: Seq[String]): Future[Seq[Option[Campaign]]]

  /**
   * Retrieve several campaigns according to a query, ordered by decreasing `createTime` (the most
   * recent campaign is returned first).
   *
   * @param query A query to filter campaigns.
   */
  def list(query: CampaignStore.Query = CampaignStore.Query()): Future[Seq[Campaign]]

  /**
   * Count campaigns according to a query.
   *
   * @param query A query to filter campaigns.
   */
  def count(query: CampaignStore.Query = CampaignStore.Query()): Future[Int]
}

object CampaignStore {

  /**
   * A query used to filter campaigns.
   *
   * @param isActive Return only (in)active campaigns.
   */
  case class Query(isActive: Option[Boolean] = None)

}
