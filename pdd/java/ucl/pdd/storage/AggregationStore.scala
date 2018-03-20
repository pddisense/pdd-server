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
import ucl.pdd.api.Aggregation

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
