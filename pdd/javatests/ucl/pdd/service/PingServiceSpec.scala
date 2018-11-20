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

import java.net.InetAddress

import com.twitter.util.{Await, Future}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.domain.{Campaign, Client, PingRequest, PingResponse, Sketch, Vocabulary}
import ucl.pdd.geocoder.NullGeocoder
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.testing.UnitSpec

/**
 * Unit tests for [[PingService]].
 */
class PingServiceSpec extends UnitSpec with BeforeAndAfterEach {
  behavior of "PingService"

  private var service: PingService = _
  private var storage: Storage = _
  private val timezone = DateTimeZone.forID("Europe/London")

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
      collectRaw = true,
      collectEncrypted = true,
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
        Vocabulary.Query(terms = Some(Seq("foot", "ball"))),
        Vocabulary.Query(exact = Some("weather")))),
      startTime = Some(at("2018-05-12T10:00:00")),
      endTime = None,
      collectRaw = true,
      collectEncrypted = false,
      delay = 0,
      graceDelay = 0,
      groupSize = 3,
      samplingRate = None)

    val clients = Seq(
      Client(
        name = s"client1",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        publicKey = s"pubkey1",
        browser = "fake-browser"),
      Client(
        name = s"client2",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        publicKey = s"pubkey2",
        browser = "fake-browser"),
      Client(
        name = s"client3",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        publicKey = s"pubkey3",
        browser = "fake-browser"))

    val sketches = Seq(
      // campaign1 - day 0
      Sketch(
        name = "sketch1",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client1",
        day = 0,
        group = 0,
        queriesCount = 1,
        rawValues = Some(Seq(4, 4)),
        encryptedValues = Some(Seq("34", "-343")),
        submitted = true,
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch2",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client2",
        day = 0,
        group = 0,
        queriesCount = 1,
        submitted = false,
        publicKey = "pubkey2"),

      // campaign1 - day 1
      Sketch(
        name = "sketch6",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client1",
        day = 1,
        group = 0,
        queriesCount = 2,
        submitted = false,
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch7",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign1",
        clientName = "client2",
        day = 1,
        queriesCount = 2,
        group = 0,
        rawValues = None,
        encryptedValues = None,
        submitted = false,
        publicKey = "pubkey2"),

      // campaign2 - day 0
      Sketch(
        name = "sketch3",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign2",
        clientName = "client1",
        day = 0,
        queriesCount = 3,
        group = 0,
        submitted = true,
        rawValues = Some(Seq(6, 1, 2, 3)),
        encryptedValues = None,
        publicKey = "pubkey1"),
      Sketch(
        name = "sketch9",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign2",
        clientName = "client3",
        day = 0,
        group = 0,
        queriesCount = 3,
        submitted = true,
        rawValues = Some(Seq(1, 0, 0, 1)),
        encryptedValues = None,
        publicKey = "pubkey3"),
      Sketch(
        name = "sketch8",
        createTime = at("2018-05-10T15:53:00"), // Value does not matter.
        campaignName = "campaign2",
        clientName = "client2",
        day = 0,
        group = 0,
        queriesCount = 3,
        submitted = false,
        publicKey = "pubkey2"))

    Await.ready(Future.join(
      Seq(storage.campaigns.create(campaign1), storage.campaigns.create(campaign2)) ++
        clients.map(storage.clients.create) ++
        sketches.map(storage.sketches.create)))

    service = new PingService(storage, NullGeocoder, timezone, testingMode = false)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Await.ready(storage.shutDown())
    storage = null
    service = null
    super.afterEach()
  }

  it should "return a response to a client" in {
    def runTests(now: String): Unit = {
      var resp = Await.result(service.apply(PingRequest("client1", InetAddress.getByName("0.0.0.0")), at(now))).get
      resp.submit should contain theSameElementsAs Seq(
        PingResponse.Command(
          sketchName = "sketch6",
          startTime = at("2018-05-12T00:00:00"),
          endTime = at("2018-05-13T00:00:00"),
          vocabulary = Vocabulary(Seq(
            Vocabulary.Query(exact = Some("foo")),
            Vocabulary.Query(exact = Some("bar")))),
          publicKeys = Seq("pubkey1", "pubkey2"),
          collectRaw = true,
          collectEncrypted = true,
          round = 1))
      // The latest version of the vocabulary is always returned.
      resp.vocabulary.queries should contain theSameElementsAs Set(
        Vocabulary.Query(exact = Some("foo")),
        Vocabulary.Query(exact = Some("bar")),
        Vocabulary.Query(terms = Some(Seq("foot", "ball"))),
        Vocabulary.Query(exact = Some("weather")))

      resp = Await.result(service.apply(PingRequest("client2", InetAddress.getByName("0.0.0.0")), at(now))).get
      resp.submit should contain theSameElementsAs Seq(
        PingResponse.Command(
          sketchName = "sketch2",
          startTime = at("2018-05-11T00:00:00"),
          endTime = at("2018-05-12T00:00:00"),
          // Vocabulary is truncated to 1 on day 1.
          vocabulary = Vocabulary(Seq(Vocabulary.Query(exact = Some("foo")))),
          publicKeys = Seq("pubkey1", "pubkey2"),
          collectRaw = true,
          collectEncrypted = true,
          round = 0),
        PingResponse.Command(
          sketchName = "sketch7",
          startTime = at("2018-05-12T00:00:00"),
          endTime = at("2018-05-13T00:00:00"),
          vocabulary = Vocabulary(Seq(
            Vocabulary.Query(exact = Some("foo")),
            Vocabulary.Query(exact = Some("bar")))),
          publicKeys = Seq("pubkey1", "pubkey2"),
          collectRaw = true,
          collectEncrypted = true,
          round = 1),
        PingResponse.Command(
          sketchName = "sketch8",
          startTime = at("2018-05-12T00:00:00"),
          endTime = at("2018-05-13T00:00:00"),
          vocabulary = Vocabulary(Seq(
            Vocabulary.Query(exact = Some("foo")),
            Vocabulary.Query(terms = Some(Seq("foot", "ball"))),
            Vocabulary.Query(exact = Some("weather")))),
          publicKeys = Seq("pubkey1", "pubkey2", "pubkey3"),
          collectRaw = true,
          collectEncrypted = false,
          round = 0))
      // The latest version of the vocabulary is always returned.
      resp.vocabulary.queries should contain theSameElementsAs Set(
        Vocabulary.Query(exact = Some("foo")),
        Vocabulary.Query(exact = Some("bar")),
        Vocabulary.Query(terms = Some(Seq("foot", "ball"))),
        Vocabulary.Query(exact = Some("weather")))

      resp = Await.result(service.apply(PingRequest("client3", InetAddress.getByName("0.0.0.0")), at(now))).get
      // It does return something (the client exists), but it has nothing to do.
      resp.submit should have size 0
      // The latest version of the vocabulary is always returned.
      resp.vocabulary.queries should contain theSameElementsAs Set(
        Vocabulary.Query(exact = Some("foo")),
        Vocabulary.Query(exact = Some("bar")),
        Vocabulary.Query(terms = Some(Seq("foot", "ball"))),
        Vocabulary.Query(exact = Some("weather")))
    }

    // Test at several times of the day (including edge cases).
    runTests("2018-05-13T11:12:34")
    runTests("2018-05-13T01:10:00")
    runTests("2018-05-13T23:59:31")
    runTests("2018-05-14T00:45:17") // Next day, before next sketches have been generated.
  }

  it should "handle a non-existing client" in {
    Await.result(service.apply(PingRequest("client100", InetAddress.getByName("0.0.0.0")), at("2018-05-13T11:12:34"))) shouldBe None
  }

  // All our operations should use the canonical timezone used by the service.
  private def at(str: String) = new DateTime(str, timezone).toInstant
}
