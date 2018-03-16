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
   * Save an aggregation. If the aggregation does not exist, it will be created. If an aggregation
   * with the same name already exists, it will be updated.
   *
   * @param aggregation An aggregation to save.
   */
  def save(aggregation: Aggregation): Future[Unit]

  /**
   * Retrieve aggregations, according to a query.
   *
   * @param query A query used to filter aggregations.
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
