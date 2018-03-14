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
import org.joda.time.{DateTime, Instant}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.api.{Aggregation, AggregationStats, Campaign, Client, Vocabulary, VocabularyQuery}
import ucl.testing.UnitSpec

abstract class AggregationStoreSpec extends StoreSpec {
  it should "manage aggregations" in {
    val agg1 = Aggregation(
      name = "agg1",
      campaignName = "campaign1",
      day = 0,
      decryptedValues = Seq(0, 2, 0, 0),
      rawValues = Seq(0, 1, 0, 0),
      stats = AggregationStats(2, 2, 1))
    val agg2 = Aggregation(
      name = "agg2",
      campaignName = "campaign1",
      day = 1,
      decryptedValues = Seq(0, 2, 0, 5),
      rawValues = Seq(0, 2, 0, 10),
      stats = AggregationStats(3, 2, 1))
    val agg3 = Aggregation(
      name = "agg3",
      campaignName = "campaign2",
      day = 0,
      decryptedValues = Seq(0, 0),
      rawValues = Seq(0, 1),
      stats = AggregationStats(2, 1, 1))

    Await.result(storage.aggregations.list(AggregationQuery(campaignName = "campaign1"))) should have size 0

    Await.result(storage.aggregations.create(agg1)) shouldBe true
    Await.result(storage.aggregations.create(agg2)) shouldBe true
    Await.result(storage.aggregations.create(agg3)) shouldBe true
    Await.result(storage.aggregations.create(agg1)) shouldBe false

    Await.result(storage.aggregations.list(AggregationQuery(campaignName = "campaign1"))) should contain theSameElementsInOrderAs Seq(agg2, agg1)
    Await.result(storage.aggregations.list(AggregationQuery(campaignName = "campaign2"))) should contain theSameElementsInOrderAs Seq(agg3)
  }
}
