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
import ucl.pdd.domain.{Campaign, Client, Vocabulary}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.strategy.NaiveGroupStrategy
import ucl.testing.UnitSpec

/**
 * Unit tests for [[CreateSketchesJob]].
 */
class CreateSketchesJobSpec extends UnitSpec with BeforeAndAfterEach {
  behavior of "CreateSketchesJob"

  private var job: CreateSketchesJob = _
  private var storage: Storage = _
  private val timezone = DateTimeZone.forID("Europe/London")
  private val now = DateTime.now(timezone).minusDays(1).withHourOfDay(12).toInstant
  private val campaign1 = Campaign(
    name = "campaign1",
    createTime = now,
    displayName = "a campaign",
    email = None,
    notes = None,
    vocabulary = Vocabulary(Seq(Vocabulary.Query(exact = Some("foo")))),
    startTime = Some(now),
    endTime = None,
    collectRaw = true,
    collectEncrypted = true,
    delay = 0,
    graceDelay = 0,
    groupSize = 2,
    samplingRate = None)

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

  it should "create sketches for several campaigns" in {
    val campaign2 = campaign1.copy(name = "campaign2")
    val clients = Seq.tabulate(5)(idx => createClient(idx))
    Await.result(Future.collect(Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++ clients.map(storage.clients.create)))

    job.execute(now.toDateTime(timezone).plusDays(2).toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 10 // 5 clients * 2 campaigns = 10 sketches.
    sketches.filter(_.campaignName == "campaign1") should have size 5
    sketches.filter(_.campaignName == "campaign2") should have size 5
    sketches.filter(_.clientName == "client1") should have size 2
    sketches.filter(_.clientName == "client2") should have size 2
    sketches.filter(_.clientName == "client3") should have size 2
    sketches.filter(_.clientName == "client4") should have size 2
    sketches.filter(_.clientName == "client5") should have size 2

    sketches.foreach { sketch =>
      sketch.encryptedValues.isEmpty shouldBe true
      sketch.rawValues.isEmpty shouldBe true
      sketch.submitted shouldBe false
      sketch.campaignName should (be("campaign1") or be("campaign2"))
      // The following line does compile despite Intellij errors.
      sketch.clientName should (be("client1") or be("client2") or be("client3") or be("client4") or be("client5"))
      sketch.day shouldBe 1
      sketch.publicKey shouldBe s"pubkey${sketch.clientName.last}"
    }
  }

  it should "honor a groups strategy" in {
    val campaign2 = campaign1.copy(name = "campaign2", groupSize = 5)
    val clients = Seq.tabulate(5)(idx => createClient(idx))
    Await.result(Future.collect(Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++ clients.map(storage.clients.create)))

    job.execute(now.toDateTime(timezone).plusDays(2).toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches.filter(_.campaignName == "campaign1").groupBy(_.group).map(_._2.size).toSeq.sorted should contain theSameElementsInOrderAs Seq(1, 2, 2)
    sketches.filter(_.campaignName == "campaign2").groupBy(_.group).map(_._2.size).toSeq.sorted should contain theSameElementsInOrderAs Seq(5)
  }

  it should "ignore inactive campaigns" in {
    val campaign2 = campaign1.copy(name = "campaign2", endTime = Some(now.minus(24 * 3600000)))
    val clients = Seq.tabulate(5)(idx => createClient(idx))
    Await.result(Future.collect(Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++ clients.map(storage.clients.create)))

    job.execute(now.toDateTime(timezone).plusDays(2).toInstant)
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 5 // Only one active campaign.
    sketches.foreach(sketch => sketch.campaignName shouldBe "campaign1")
  }

  private def createClient(index: Int) =
    Client(
      name = s"client${index + 1}",
      createTime = now,
      publicKey = s"pubkey${index + 1}",
      browser = "fake-browser")
}
