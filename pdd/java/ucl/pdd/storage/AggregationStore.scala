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
import ucl.pdd.domain.Aggregation

trait AggregationStore {
  /**
   * Persist a new aggregation, if no other aggregation with the same name exists.
   *
   * @param aggregation An aggregation to create.
   * @return Whether the aggregation was successfully created.
   */
  def create(aggregation: Aggregation): Future[Boolean]

  /**
   * Replace an existing aggregation with a new one, if such an aggregation with the same name
   * already exists. All fields will be modified according to the values of the new aggregation.
   *
   * @param aggregation An aggregation to update.
   * @return Whether the aggregation was successfully replaced.
   */
  def replace(aggregation: Aggregation): Future[Boolean]

  /**
   * Retrieve a single aggregation by its name, if it exists.
   *
   * @param name An aggregation name.
   */
  def get(name: String): Future[Option[Aggregation]]

  /**
   * Retrieve several aggregations according to a query, ordered by increasing `day`.
   *
   * @param query A query to filter aggregations.
   */
  def list(query: AggregationStore.Query): Future[Seq[Aggregation]]

  /**
   * Delete several aggregations according to a query.
   *
   * @param query A query to filter aggregations.
   */
  def delete(query: AggregationStore.Query): Future[Unit]
}

object AggregationStore {

  /**
   * A query used to filter aggregations. The `campaignName` field has to be specified, by design,
   * as it is meaningless to retrieve all aggregations across all campaigns.
   *
   * @param campaignName Return only aggregations belonging to a given campaign.
   */
  case class Query(campaignName: String)

}
