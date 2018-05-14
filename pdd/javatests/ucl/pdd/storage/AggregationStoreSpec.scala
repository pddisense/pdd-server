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

import com.twitter.util.{Await, Future}
import ucl.pdd.domain.Aggregation

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
      stats = Aggregation.Stats(2, 2, 1)),
    Aggregation(
      name = "agg2",
      campaignName = "campaign1",
      day = 1,
      decryptedValues = Seq(0, 2, 0, 5),
      rawValues = Seq(0, 2, 0, 10),
      stats = Aggregation.Stats(3, 2, 1)),
    Aggregation(
      name = "agg3",
      campaignName = "campaign2",
      day = 0,
      decryptedValues = Seq(0, 0),
      rawValues = Seq(0, 1),
      stats = Aggregation.Stats(2, 1, 1)))

  it should "create and retrieve aggregations" in {
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should have size 0

    aggregations.foreach(agg => Await.result(storage.aggregations.create(agg)) shouldBe true)
    Await.result(storage.aggregations.create(aggregations.head)) shouldBe false

    Await.result(storage.aggregations.get("agg1")) shouldBe Some(aggregations(0))
    Await.result(storage.aggregations.get("agg2")) shouldBe Some(aggregations(1))
    Await.result(storage.aggregations.get("agg3")) shouldBe Some(aggregations(2))

    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should contain theSameElementsInOrderAs Seq(aggregations(1), aggregations(0))
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign2"))) should contain theSameElementsInOrderAs Seq(aggregations(2))
  }

  it should "replace aggregations" in {
    Await.result(storage.aggregations.replace(aggregations.head)) shouldBe false

    Await.result(Future.join(aggregations.map(storage.aggregations.create)))

    val newAgg1 = aggregations(0).copy(day = 2)
    Await.result(storage.aggregations.replace(newAgg1)) shouldBe true
    Await.result(storage.aggregations.get("agg1")) shouldBe Some(newAgg1)
    Await.result(storage.aggregations.get("agg2")) shouldBe Some(aggregations(1))
    Await.result(storage.aggregations.get("agg3")) shouldBe Some(aggregations(2))
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should contain theSameElementsInOrderAs Seq(newAgg1, aggregations(1))
  }

  it should "delete aggregations" in {
    Await.result(Future.join(aggregations.map(storage.aggregations.create)))

    Await.result(storage.aggregations.delete(AggregationStore.Query(campaignName = "campaign1")))
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1"))) should have size 0
    Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign2"))) should contain theSameElementsInOrderAs Seq(aggregations(2))
  }
}
