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

import com.twitter.util.Await
import ucl.pdd.api.{Aggregation, AggregationStats}

/**
 * Common unit tests for implementations of [[AggregationStore]].
 */
abstract class AggregationStoreSpec extends StoreSpec {
  private[this] val aggregations = Seq(
    Aggregation(
      name = "agg1",
      campaignName = "campaign1",
      day = 0,
      decryptedValues = Seq(0, 2, 0, 0),
      rawValues = Seq(0, 1, 0, 0),
      stats = AggregationStats(2, 2, 1)),
    Aggregation(
      name = "agg2",
      campaignName = "campaign1",
      day = 1,
      decryptedValues = Seq(0, 2, 0, 5),
      rawValues = Seq(0, 2, 0, 10),
      stats = AggregationStats(3, 2, 1)),
    Aggregation(
      name = "agg3",
      campaignName = "campaign2",
      day = 0,
      decryptedValues = Seq(0, 0),
      rawValues = Seq(0, 1),
      stats = AggregationStats(2, 1, 1)))

  it should "create and retrieve aggregations" in {
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should have size 0

    aggregations.foreach(agg => Await.result(storage.aggregations.save(agg)))

    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should contain theSameElementsInOrderAs Seq(aggregations(1), aggregations(0))
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign2"))) should contain theSameElementsInOrderAs Seq(aggregations(2))
  }

  it should "replace aggregations" in {
    aggregations.foreach(agg => Await.result(storage.aggregations.save(agg)))
    val newAgg1 = aggregations(0).copy(day = 2)
    Await.result(storage.aggregations.save(newAgg1))

    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should contain theSameElementsInOrderAs Seq(newAgg1, aggregations(1))
  }
}
