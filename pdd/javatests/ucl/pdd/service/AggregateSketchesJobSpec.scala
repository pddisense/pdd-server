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

package ucl.pdd.service

import com.twitter.util.{Await, Future}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.domain.{Aggregation, Campaign, Sketch, Vocabulary}
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

  override def beforeEach(): Unit = {
    storage = MemoryStorage.empty
    Await.ready(storage.startUp())

    val campaign1 = Campaign(
      name = "campaign1",
      createTime = at("2018-05-10T15:53:00"), // Value does not matter.
      displayName = "a campaign",
      email = None,
      notes = None,
      vocabulary = Vocabulary(Seq(
        Vocabulary.Query(exact = Some("foo")),
        Vocabulary.Query(exact = Some("bar")))),
      startTime = Some(at("2018-05-11T15:00:00")),
      endTime = None,
      delay = 0,
      graceDelay = 0,
      groupSize = 3,
      samplingRate = None)
    val campaign2 = Campaign(
      name = "campaign2",
      createTime = at("2018-05-10T15:53:00"), // Value does not matter.
      displayName = "another campaign",
      email = None,
      notes = None,
      vocabulary = Vocabulary(Seq(
        Vocabulary.Query(exact = Some("foo")),
        Vocabulary.Query(exact = Some("bar")),
        Vocabulary.Query(exact = Some("weather")))),
      startTime = Some(at("2018-05-12T10:00:00")),
      endTime = None,
      delay = 0,
      graceDelay = 0,
      groupSize = 3,
      samplingRate = None)

    Await.ready(Future.join(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)))

    job = new AggregateSketchesJob(storage)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Await.ready(storage.shutDown())
    storage = null
    job = null
    super.afterEach()
  }

  it should "aggregate sketches" in {
    val sketches = Seq(
      // campaign 1 - day 0
      Sketch(
        name = "sketch1",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client1",
        day = 0,
        group = 0,
        submitted = true,
        queriesCount = 2,
        rawValues = Some(Seq(2, 0, 2)),
        encryptedValues = Some(Seq("-3", "-1", "0")),
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch2",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client2",
        day = 0,
        submitted = true,
        queriesCount = 2,
        rawValues = Some(Seq(1, 0, 1)),
        encryptedValues = Some(Seq("10", "2", "-1")),
        group = 0,
        publicKey = "pubkey2"),
      Sketch(
        name = "sketch3",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client3",
        day = 0,
        group = 0,
        queriesCount = 2,
        submitted = true,
        rawValues = Some(Seq(3, 1, 2)),
        encryptedValues = Some(Seq("-1", "0", "6")),
        publicKey = "pubkey3"),
      Sketch(
        name = "sketch4",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client4",
        day = 0,
        submitted = true,
        group = 1,
        queriesCount = 2,
        rawValues = Some(Seq(1, 1, 0)),
        encryptedValues = Some(Seq("2", "0", "-3")),
        publicKey = "pubkey4"),
      Sketch(
        name = "sketch5",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client5",
        day = 0,
        group = 1,
        queriesCount = 2,
        submitted = false,
        publicKey = "pubkey5"),

      // campaign 1 - day 1
      Sketch(
        name = "sketch6",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client1",
        day = 1,
        group = 0,
        queriesCount = 2,
        submitted = true,
        rawValues = Some(Seq(4, 2, 2)),
        encryptedValues = Some(Seq("-1", "2", "-40")),
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch7",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client2",
        day = 1,
        group = 0,
        queriesCount = 2,
        submitted = true,
        rawValues = Some(Seq(1, 1, 0)),
        encryptedValues = Some(Seq("6", "1", "42")),
        publicKey = "pubkey2"),

      // campaign 2 - day 0
      Sketch(
        name = "sketch8",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign2",
        clientName = "client2",
        day = 0,
        group = 0,
        queriesCount = 3,
        rawValues = Some(Seq(6, 1, 2, 3)),
        submitted = true,
        publicKey = "pubkey2"),
      Sketch(
        name = "sketch9",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign2",
        clientName = "client3",
        day = 0,
        group = 0,
        submitted = true,
        queriesCount = 3,
        rawValues = Some(Seq(5, 1, 0, 4)),
        publicKey = "pubkey3"))
    Await.ready(Future.join(sketches.map(storage.sketches.create)))

    job.execute(at("2018-05-13T11:12:34"))

    var res = Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1")))
    res should contain theSameElementsAs Seq(
      Aggregation(
        name = "campaign1-0",
        campaignName = "campaign1",
        day = 0,
        rawValues = Seq(7, 2, 5),
        decryptedValues = Seq(6, 1, 5),
        stats = Aggregation.Stats(activeCount = 5, submittedCount = 4, decryptedCount = 3)))

    res = Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign2")))
    res should have size 0

    job.execute(at("2018-05-14T11:12:34"))

    res = Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign1")))
    res should contain theSameElementsAs Seq(
      Aggregation(
        name = "campaign1-0",
        campaignName = "campaign1",
        day = 0,
        rawValues = Seq(7, 2, 5),
        decryptedValues = Seq(6, 1, 5),
        stats = Aggregation.Stats(activeCount = 5, submittedCount = 4, decryptedCount = 3)),
      Aggregation(
        name = "campaign1-1",
        campaignName = "campaign1",
        day = 1,
        rawValues = Seq(5, 3, 2),
        decryptedValues = Seq(5, 3, 2),
        stats = Aggregation.Stats(activeCount = 2, submittedCount = 2, decryptedCount = 2)))

    res = Await.result(storage.aggregations.list(AggregationStore.Query(campaignName = "campaign2")))
    res should contain theSameElementsAs Seq(
      Aggregation(
        name = "campaign2-0",
        campaignName = "campaign2",
        day = 0,
        rawValues = Seq(11, 2, 2, 7),
        decryptedValues = Seq.empty,
        stats = Aggregation.Stats(activeCount = 2, submittedCount = 2, decryptedCount = 0)))
  }

  // All our operations should use the canonical timezone used by the service.
  private def at(str: String) = new DateTime(str, timezone).toInstant
}
