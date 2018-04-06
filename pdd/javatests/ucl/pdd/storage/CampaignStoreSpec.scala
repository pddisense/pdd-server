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
      email = Seq.empty,
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
      email = Seq("v@ucl.ac.uk", "p@ucl.ac.ul"),
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

    campaigns.foreach(campaign => Await.result(storage.campaigns.create(campaign)) shouldBe true)
    Await.result(storage.campaigns.count()) shouldBe 2
    Await.result(storage.campaigns.count(CampaignStore.Query(isActive = Some(true)))) shouldBe 1
    Await.result(storage.campaigns.count(CampaignStore.Query(isActive = Some(false)))) shouldBe 1
  }

  it should "replace campaigns" in {
    Await.result(storage.campaigns.replace(campaigns.head)) shouldBe false

    campaigns.foreach(campaign => Await.result(storage.campaigns.create(campaign)) shouldBe true)

    val newCampaign1 = campaigns(0).copy(startTime = Some(now().minus(5000)))
    Await.result(storage.campaigns.replace(newCampaign1)) shouldBe true
    Await.result(storage.campaigns.get("campaign1")) shouldBe Some(newCampaign1)
    Await.result(storage.campaigns.get("campaign2")) shouldBe Some(campaigns(1))
    Await.result(storage.campaigns.list()) should contain theSameElementsInOrderAs Seq(campaigns(1), newCampaign1)
  }

  it should "retrieve campaigns in batch" in {
    campaigns.foreach(campaign => Await.result(storage.campaigns.create(campaign)) shouldBe true)

    Await.result(storage.campaigns.batchGet(Seq("campaign42", "campaign1", "campaign2"))) should contain theSameElementsInOrderAs Seq(None, Some(campaigns(0)), Some(campaigns(1)))
  }
}
