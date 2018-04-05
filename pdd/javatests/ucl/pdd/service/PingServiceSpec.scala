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
import org.joda.time.{DateTime, DateTimeZone, Instant}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.api.{Campaign, Client, Sketch, Vocabulary, VocabularyQuery}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.testing.UnitSpec

/**
 * Unit tests for [[PingService]].
 */
class PingServiceSpec extends UnitSpec with BeforeAndAfterEach {
  behavior of "PingService"

  private[this] var service: PingService = _
  private[this] var storage: Storage = _
  private[this] val timezone = DateTimeZone.forID("Europe/London")
  private[this] val now = DateTime.now(timezone)

  override def beforeEach(): Unit = {
    storage = new MemoryStorage
    Await.ready(storage.startUp())
    val createTime = now.minusDays(1).withHourOfDay(12).toInstant
    val campaign1 = Campaign(
      name = "campaign1",
      createTime = createTime,
      displayName = "a campaign",
      email = Seq.empty,
      vocabulary = Vocabulary(Seq(VocabularyQuery(exact = Some("foo")))),
      startTime = Some(createTime),
      endTime = None,
      collectRaw = true,
      collectEncrypted = true,
      delay = 0,
      graceDelay = 0,
      groupSize = 3,
      samplingRate = None)
    val campaign2 = campaign1.copy(name = "campaign2", collectRaw = false)
    val clients = Seq(
      Client(
        name = s"client1",
        createTime = createTime,
        publicKey = s"pubkey1",
        browser = "fake-browser",
        leaveTime = None),
      Client(
        name = s"client2",
        createTime = createTime,
        publicKey = s"pubkey2",
        browser = "fake-browser",
        leaveTime = None),
      Client(
        name = s"client3",
        createTime = createTime,
        publicKey = s"pubkey3",
        browser = "fake-browser",
        leaveTime = None)
    )
    Await.ready(Future.join(
      Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++
        clients.map(storage.clients.create)))

    service = new PingService(storage, timezone, testingMode = false)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Await.ready(storage.shutDown())
    storage = null
    service = null
    super.afterEach()
  }

  it should "return commands to a client" in {
    val sketches = Seq(
      Sketch(
        name = "sketch1",
        campaignName = "campaign1",
        clientName = "client1",
        day = 0,
        group = 0,
        rawValues = Some(Seq.empty), // Not important
        encryptedValues = Some(Seq.empty), // Not important
        submitted = true, // Not important
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch2",
        campaignName = "campaign1",
        clientName = "client2",
        day = 0,
        group = 0,
        submitted = false,
        publicKey = "pubkey2"),

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
        rawValues = Some(Seq.empty), // Not important
        encryptedValues = Some(Seq.empty), // Not important
        submitted = true, // Not important
        publicKey = "pubkey2"),

      Sketch(
        name = "sketch3",
        campaignName = "campaign2",
        clientName = "client1",
        day = 0,
        group = 0,
        submitted = false,
        publicKey = "pubkey2"),
      Sketch(
        name = "sketch8",
        campaignName = "campaign2",
        clientName = "client2",
        day = 0,
        group = 0,
        submitted = false,
        publicKey = "pubkey2"),
      Sketch(
        name = "sketch9",
        campaignName = "campaign2",
        clientName = "client3",
        day = 0,
        group = 0,
        submitted = true, // Not important
        rawValues = Some(Seq.empty), // Not important
        encryptedValues = Some(Seq.empty), // Not important
        publicKey = "pubkey3"))
    Await.result(Future.collect(sketches.map(storage.sketches.create)))

    val maybeResp = Await.result(service.apply("client1", now.toInstant))
    maybeResp.isDefined shouldBe true
    val resp = maybeResp.get

    resp.submit should have size 2
  }

  it should "return nothing if the client does not exist" in {
    val maybeResp = Await.result(service.apply("client100", now.toInstant))
    maybeResp.isDefined shouldBe false
  }
}
