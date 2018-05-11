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
import ucl.pdd.api.{Campaign, Vocabulary, VocabularyQuery}

/**
 * Common unit tests for implementations of [[CampaignStore]].
 */
abstract class CampaignStoreSpec extends StoreSpec {
  private[this] val campaigns = Seq(
    Campaign(
      name = "campaign1",
      createTime = now(),
      vocabulary = Vocabulary(),
      displayName = "first campaign",
      email = None,
      notes = Some("some notes"),
      startTime = None,
      endTime = None,
      collectRaw = true,
      collectEncrypted = false,
      delay = 0,
      graceDelay = 0,
      groupSize = 0,
      samplingRate = None),
    Campaign(
      name = "campaign2",
      createTime = now().plus(1000),
      vocabulary = Vocabulary(queries = Seq(VocabularyQuery(exact = Some("foo")), VocabularyQuery(exact = Some("bar")), VocabularyQuery(terms = Some(Seq("a", "b"))))),
      displayName = "second campaign",
      email = Some("v@ucl.ac.uk"),
      notes = None,
      startTime = Some(now()),
      endTime = Some(now().plus(3600 * 1000 * 24 * 5)),
      collectRaw = false,
      collectEncrypted = true,
      delay = 1,
      graceDelay = 2,
      groupSize = 5,
      samplingRate = Some(.5)))

  it should "create and retrieve campaigns" in {
    Await.result(storage.campaigns.get("campaign1")) shouldBe None
    Await.result(storage.campaigns.list()) shouldBe Seq.empty

    campaigns.foreach(campaign => Await.result(storage.campaigns.create(campaign)) shouldBe true)
    Await.result(storage.campaigns.create(campaigns.head)) shouldBe false

    Await.result(storage.campaigns.get("campaign1")) shouldBe Some(campaigns(0))
    Await.result(storage.campaigns.get("campaign2")) shouldBe Some(campaigns(1))

    Await.result(storage.campaigns.list()) should contain theSameElementsInOrderAs Seq(campaigns(1), campaigns(0))
    Await.result(storage.campaigns.list(CampaignStore.Query(isActive = Some(true)))) should contain theSameElementsInOrderAs Seq(campaigns(1))
    Await.result(storage.campaigns.list(CampaignStore.Query(isActive = Some(false)))) should contain theSameElementsInOrderAs Seq(campaigns(0))
  }

  it should "count campaigns" in {
    Await.result(storage.campaigns.count()) shouldBe 0
    Await.result(Future.join(campaigns.map(storage.campaigns.create)))

    Await.result(storage.campaigns.count()) shouldBe 2
    Await.result(storage.campaigns.count(CampaignStore.Query(isActive = Some(true)))) shouldBe 1
    Await.result(storage.campaigns.count(CampaignStore.Query(isActive = Some(false)))) shouldBe 1
  }

  it should "replace campaigns" in {
    Await.result(storage.campaigns.replace(campaigns.head)) shouldBe false
    Await.result(Future.join(campaigns.map(storage.campaigns.create)))

    val newCampaign1 = campaigns(0).copy(
      startTime = Some(now().minus(5000)),
      notes = Some("new notes"),
      vocabulary = Vocabulary(queries = Seq(VocabularyQuery(exact = Some("bar")), VocabularyQuery(terms = Some(Seq("bar", "barbar"))))),
      email = Some("other@ucl.ac.uk"),
      delay = 3,
      graceDelay = 5,
      groupSize = 100,
      samplingRate = Some(.5))
    Await.result(storage.campaigns.replace(newCampaign1)) shouldBe true
    Await.result(storage.campaigns.get("campaign1")) shouldBe Some(newCampaign1)
    Await.result(storage.campaigns.get("campaign2")) shouldBe Some(campaigns(1))
    Await.result(storage.campaigns.list()) should contain theSameElementsInOrderAs Seq(campaigns(1), newCampaign1)
  }

  it should "delete campaigns" in {
    campaigns.foreach(campaign => Await.result(storage.campaigns.create(campaign)) shouldBe true)

    Await.result(storage.campaigns.delete("campaign1"))
    Await.result(storage.campaigns.get("campaign1")) shouldBe None
    Await.result(storage.campaigns.get("campaign2")) shouldBe Some(campaigns(1))
    Await.result(storage.campaigns.list()) should contain theSameElementsInOrderAs Seq(campaigns(1))
  }

  it should "retrieve campaigns in batch" in {
    campaigns.foreach(campaign => Await.result(storage.campaigns.create(campaign)) shouldBe true)

    Await.result(storage.campaigns.batchGet(Seq("campaign42", "campaign1", "campaign2"))) should contain theSameElementsInOrderAs Seq(None, Some(campaigns(0)), Some(campaigns(1)))
  }
}
