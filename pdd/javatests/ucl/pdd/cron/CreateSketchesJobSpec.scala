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

package ucl.pdd.cron

import com.twitter.util.{Await, Future}
import org.joda.time.{DateTime, DateTimeZone, Instant}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.api.{Campaign, Client, Vocabulary, VocabularyQuery}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.strategy.RoundRobinStrategy
import ucl.testing.UnitSpec

/**
 * Unit tests for [[CreateSketchesJob]].
 */
class CreateSketchesJobSpec extends UnitSpec with BeforeAndAfterEach {
  behavior of "CreateSketchesJob"

  private[this] var job: CreateSketchesJob = _
  private[this] var storage: Storage = _
  private[this] val timezone = DateTimeZone.forID("Europe/London")
  private[this] val now = DateTime.now(timezone).withHourOfDay(12).withMinuteOfHour(15).toInstant
  private[this] val campaign1 = Campaign(
    name = "campaign1",
    createTime = now,
    displayName = "a campaign",
    email = Seq.empty,
    vocabulary = Vocabulary(Seq(VocabularyQuery(exact = Some("foo")))),
    startTime = Some(now.minus(1000)),
    endTime = None,
    collectRaw = true,
    collectEncrypted = true,
    delay = 0,
    graceDelay = 0,
    groupSize = 2,
    samplingRate = None)

  override def beforeEach(): Unit = {
    storage = new MemoryStorage
    job = new CreateSketchesJob(storage, new RoundRobinStrategy, timezone)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    storage = null
    job = null
    super.afterEach()
  }

  it should "create sketches for several campaigns" in {
    val campaign2 = campaign1.copy(name = "campaign2")
    val clients = Seq.tabulate(5)(idx => createClient(idx ))
    Await.result(Future.collect(Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++ clients.map(storage.clients.create)))

    job.execute(new FakeJobExecutionContext(now.toDateTime(timezone).plusDays(2).toInstant))
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
      sketch.submitTime.isEmpty shouldBe true
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

    job.execute(new FakeJobExecutionContext(now.toDateTime(timezone).plusDays(2).toInstant))
    val sketches = Await.result(storage.sketches.list())

    sketches.filter(_.campaignName == "campaign1").groupBy(_.group).map(_._2.size).toSeq.sorted should contain theSameElementsInOrderAs Seq(1, 2, 2)
    sketches.filter(_.campaignName == "campaign2").groupBy(_.group).map(_._2.size).toSeq.sorted should contain theSameElementsInOrderAs Seq(5)
  }

  it should "ignore inactive campaigns" in {
    val campaign2 = campaign1.copy(name = "campaign2", endTime = Some(now.minus(24 * 3600000)))
    val clients = Seq.tabulate(5)(idx => createClient(idx ))
    Await.result(Future.collect(Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++ clients.map(storage.clients.create)))

    job.execute(new FakeJobExecutionContext(now.toDateTime(timezone).plusDays(2).toInstant))
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 5 // Only one active campaign.
    sketches.foreach(sketch => sketch.campaignName shouldBe "campaign1")
  }

  it should "ignore inactive clients" in {
    val clients = Seq.tabulate(3)(idx => createClient(idx)) ++ Seq.tabulate(2)(idx => createClient(idx + 3, active = false))
    Await.result(Future.collect(storage.campaigns.create(campaign1) +: clients.map(storage.clients.create)))

    job.execute(new FakeJobExecutionContext(now.toDateTime(timezone).plusDays(2).toInstant))
    val sketches = Await.result(storage.sketches.list())

    sketches should have size 3 // Only three active clients.
    sketches.foreach { sketch =>
      // The following line does compile despite Intellij errors.
      sketch.clientName should (be("client1") or be("client2") or be("client3"))
    }
  }

  private def createClient(index: Int, active: Boolean = true) =
    Client(
      name = s"client${index + 1}",
      createTime = now,
      publicKey = s"pubkey${index + 1}",
      browser = "fake-browser",
      leaveTime = if (active) None else Some(new Instant(1)))
}
