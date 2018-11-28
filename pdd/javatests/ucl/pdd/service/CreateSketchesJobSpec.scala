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
import org.joda.time.{DateTime, DateTimeZone, Instant}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import ucl.pdd.domain.{Activity, Campaign, Client, Vocabulary}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.strategy.NaiveGroupStrategy
import ucl.testing.UnitSpec

/**
 * Unit tests for [[CreateSketchesJob]].
 */
class CreateSketchesJobSpec extends UnitSpec with BeforeAndAfterEach with BeforeAndAfterAll {
  behavior of "CreateSketchesJob"

  private val MillisPerDay = 24 * 3600000
  private var job: CreateSketchesJob = _
  private var storage: Storage = _
  private var now: Instant = _

  override def beforeAll(): Unit = {
    // Normally done in ServiceModule, need to be done for tests as well.
    DateTimeZone.setDefault(DateTimeZone.forID("Europe/London"))
    now = DateTime.now().withHourOfDay(12).toInstant
  }

  override def beforeEach(): Unit = {
    storage = MemoryStorage.empty
    job = new CreateSketchesJob(storage, NaiveGroupStrategy)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    storage = null
    job = null
    super.afterEach()
  }

  it should "create sketches" in {
    Await.result(Future.collect(
      Seq(createActiveCampaign(0, now.minus(MillisPerDay))) ++
        Seq.tabulate(5)(idx => createActiveClient(idx, now.minus(MillisPerDay)))))

    job.execute(now.toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 5 // One for each client
    sketches.map(_.clientName) should contain theSameElementsAs Set("client1", "client2", "client3", "client4", "client5")
    sketches.foreach { sketch =>
      sketch.encryptedValues.isEmpty shouldBe true
      sketch.rawValues.isEmpty shouldBe true
      sketch.submitted shouldBe false
      sketch.campaignName shouldBe "campaign1"
      sketch.day shouldBe 0
      sketch.publicKey shouldBe s"pubkey${sketch.clientName.last}"
    }
    sketches.groupBy(_.group).map(_._2.size).toSeq should contain theSameElementsAs Seq(2, 2, 1)
  }

  it should "honour a group size" in {
    Await.result(Future.collect(
      Seq(createActiveCampaign(0, now.minus(MillisPerDay), groupSize = 4)) ++
        Seq.tabulate(10)(idx => createActiveClient(idx, now.minus(MillisPerDay)))))

    job.execute(now.toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches.groupBy(_.group).map(_._2.size).toSeq should contain theSameElementsAs Seq(4, 4, 2)
  }

  it should "not create any sketches before a day is elapsed" in {
    Await.result(Future.collect(
      Seq(createActiveCampaign(0, now.minus(10000))) ++
        Seq.tabulate(5)(idx => createActiveClient(idx, now.minus(MillisPerDay)))))

    job.execute(now.toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 0
  }

  it should "create sketches for several campaigns" in {
    Await.result(Future.collect(
      Seq.tabulate(2)(idx => createActiveCampaign(idx, now.minus(MillisPerDay))) ++
        Seq.tabulate(5)(idx => createActiveClient(idx, now.minus(MillisPerDay)))))

    job.execute(now.toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 10 // 5 clients * 2 campaigns
    sketches.filter(_.campaignName == "campaign1") should have size 5
    sketches.filter(_.campaignName == "campaign2") should have size 5
    sketches.filter(_.clientName == "client1") should have size 2
    sketches.filter(_.clientName == "client2") should have size 2
    sketches.filter(_.clientName == "client3") should have size 2
    sketches.filter(_.clientName == "client4") should have size 2
    sketches.filter(_.clientName == "client5") should have size 2
  }

  it should "ignore inactive campaigns" in {
    Await.result(Future.collect(
      Seq(
        createActiveCampaign(0, now.minus(MillisPerDay)),
        createInactiveCampaign(1)) ++
        Seq.tabulate(5)(idx => createActiveClient(idx, now.minus(MillisPerDay)))))

    job.execute(now.toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 5
    sketches.map(_.campaignName).toSet should contain theSameElementsAs Set("campaign1")
  }

  private def createActiveClient(index: Int, at: Instant) = {
    val f1 = storage.clients.create(Client(
      name = s"client${index + 1}",
      createTime = now,
      publicKey = s"pubkey${index + 1}",
      browser = "fake-browser"))
    val f2 = storage.activity.create(Activity(s"client${index + 1}", at))
    Future.join(Seq(f1, f2))
  }

  private def createActiveCampaign(index: Int, at: Instant, groupSize: Int = 2) = {
    storage.campaigns.create(Campaign(
      name = s"campaign${index + 1}",
      createTime = at,
      displayName = s"campaign ${index + 1}",
      vocabulary = Vocabulary(Seq(Vocabulary.Query(exact = Some("foo")))),
      startTime = Some(at),
      endTime = None,
      collectRaw = true,
      collectEncrypted = true,
      delay = 0,
      graceDelay = 0,
      groupSize = groupSize))
  }

  private def createInactiveCampaign(index: Int) = {
    storage.campaigns.create(Campaign(
      name = s"campaign${index + 1}",
      createTime = Instant.now(),
      displayName = s"campaign ${index + 1}",
      vocabulary = Vocabulary(Seq(Vocabulary.Query(exact = Some("foo")))),
      collectRaw = true,
      collectEncrypted = true,
      delay = 0,
      graceDelay = 0,
      groupSize = 2))
  }
}
