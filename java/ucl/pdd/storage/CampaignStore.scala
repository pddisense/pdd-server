/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Campaign

trait CampaignStore {
  def create(campaign: Campaign): Future[Boolean]

  def replace(campaign: Campaign): Future[Boolean]

  def list(query: CampaignQuery = CampaignQuery()): Future[Seq[Campaign]]

  def get(name: String): Future[Option[Campaign]]

  def batchGet(names: Seq[String]): Future[Seq[Option[Campaign]]] = Future.collect(names.map(get))
}

case class CampaignQuery(isActive: Option[Boolean] = None) {
  def matches(campaign: Campaign): Boolean = {
    isActive.forall(campaign.isActive == _)
  }
}
