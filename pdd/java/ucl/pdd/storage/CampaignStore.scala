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

package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.domain.Campaign

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
   * Delete an existing campaign.
   *
   * @param name A campaign name.
   */
  def delete(name: String): Future[Unit]

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
