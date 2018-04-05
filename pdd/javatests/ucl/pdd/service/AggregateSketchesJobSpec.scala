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

package ucl.pdd.service

import com.twitter.util.{Await, Future}
import org.joda.time.{DateTime, DateTimeZone, Duration}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.api.{Campaign, Sketch, Vocabulary, VocabularyQuery}
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.storage.{AggregationStore, Storage}
import ucl.testing.UnitSpec

/**
 * Unit tests for [[AggregateSketchesJob]].
 */
class AggregateSketchesJobSpec extends UnitSpec with BeforeAndAfterEach {
  behavior of "AggregateSketchesJob"

  private[this] var job: AggregateSketchesJob = _
  private[this] var storage: Storage = _
  private[this] val timezone = DateTimeZone.forID("Europe/London")
  private[this] val now = DateTime.now(timezone)

  override def beforeEach(): Unit = {
    storage = new MemoryStorage
    Await.ready(storage.startUp())
    val startTime = now.minusDays(1).withTimeAtStartOfDay().toInstant
    val campaign1 = Campaign(
      name = "campaign1",
      createTime = now.toInstant,
      displayName = "a campaign",
      email = Seq.empty,
      vocabulary = Vocabulary(Seq(VocabularyQuery(exact = Some("foo")))),
      startTime = Some(startTime),
      endTime = None,
      collectRaw = true,
      collectEncrypted = true,
      delay = 0,
      graceDelay = 0,
      groupSize = 3,
      samplingRate = None)
    val campaign2 = campaign1.copy(name = "campaign2")
    Await.ready(Future.join(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)))

    job = new AggregateSketchesJob(storage, timezone, testingMode = false)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Await.ready(storage.shutDown())
    storage = null
    job = null
    super.afterEach()
  }

  it should "aggregate sketches for several campaigns" in {
    val sketches = Seq(
      Sketch(
        name = "sketch1",
        campaignName = "campaign1",
        clientName = "client1",
        day = 0,
        group = 0,
        submitted = true,
        rawValues = Some(Seq(1, 0, 2)),
        encryptedValues = Some(Seq("-1", "0", "1")),
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch2",
        campaignName = "campaign1",
        clientName = "client2",
        day = 0,
        submitted = true,
        rawValues = Some(Seq(0, 0, 1)),
        encryptedValues = Some(Seq("1", "1", "-2")),
        group = 0,
        publicKey = "pubkey2"),
      Sketch(
        name = "sketch3",
        campaignName = "campaign1",
        clientName = "client3",
        day = 0,
        group = 0,
        submitted = true,
        rawValues = Some(Seq(0, 1, 2)),
        encryptedValues = Some(Seq("2", "0", "6")),
        publicKey = "pubkey3"),
      Sketch(
        name = "sketch4",
        campaignName = "campaign1",
        clientName = "client4",
        day = 0,
        submitted = true,
        group = 1,
        rawValues = Some(Seq(0, 1, 0)),
        encryptedValues = Some(Seq("2", "0", "-3")),
        publicKey = "pubkey4"),
      Sketch(
        name = "sketch5",
        campaignName = "campaign1",
        clientName = "client5",
        day = 0,
        group = 1,
        submitted = false,
        publicKey = "pubkey5"),

      Sketch(
        name = "sketch6",
        campaignName = "campaign1",
        clientName = "client1",
        day = 1,
        group = 0,
        submitted = false,
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch7",
        campaignName = "campaign1",
        clientName = "client2",
        day = 1,
        group = 0,
        submitted = false,
        publicKey = "pubkey2"),

      Sketch(
        name = "sketch8",
        campaignName = "campaign2",
        clientName = "client2",
        day = 0,
        group = 0,
        rawValues = Some(Seq(0, 1, 2)),
        encryptedValues = Some(Seq("2", "0", "6")),
        submitted = true,
        publicKey = "pubkey2"),
      Sketch(
        name = "sketch9",
        campaignName = "campaign2",
        clientName = "client3",
        day = 0,
        group = 0,
        submitted = true,
        rawValues = Some(Seq(1, 1, 0)),
        encryptedValues = Some(Seq("-1", "2", "-4")),
        publicKey = "pubkey3"))
    Await.result(Future.collect(sketches.map(storage.sketches.create)))

    job.execute(now.toInstant)

    val agg1 = Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1")))
    agg1 should have size 1
    agg1.head.day shouldBe 0
    agg1.head.rawValues shouldBe Seq(1, 2, 5)
    agg1.head.decryptedValues shouldBe Seq(2, 1, 5)
    agg1.head.stats.activeCount shouldBe 5
    agg1.head.stats.submittedCount shouldBe 4
    agg1.head.stats.decryptedCount shouldBe 3

    val agg2 = Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign2")))
    agg2 should have size 1
    agg2.head.day shouldBe 0
    agg2.head.rawValues shouldBe Seq(1, 2, 2)
    agg2.head.decryptedValues shouldBe Seq(1, 2, 2)
    agg2.head.stats.activeCount shouldBe 2
    agg2.head.stats.submittedCount shouldBe 2
    agg2.head.stats.decryptedCount shouldBe 2
  }
}
