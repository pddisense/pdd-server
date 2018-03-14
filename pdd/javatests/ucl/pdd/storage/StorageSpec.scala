package ucl.pdd.storage

import com.twitter.util.Await
import org.joda.time.{DateTime, Instant}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.api.{Campaign, Client, Vocabulary, VocabularyQuery}
import ucl.testing.UnitSpec

abstract class StorageSpec extends UnitSpec with BeforeAndAfterEach {
  private[this] var storage: Storage = _

  protected def createStorage: Storage

  override def beforeEach(): Unit = {
    storage = createStorage
    storage.startAsync().awaitRunning()

    super.beforeEach()
  }

  override def afterEach(): Unit = {
    storage.stopAsync().awaitTerminated()
    storage = null

    super.afterEach()
  }

  it should "manage clients" in {
    val storage = createStorage

    val client1 = Client(
      name = "client1",
      createTime = now(),
      browser = "scalatest",
      publicKey = "foobar==")
    val client2 = Client(
      name = "client2",
      createTime = now().plus(1000),
      browser = "scalatest",
      publicKey = "foobar==",
      externalName = Some("foo"),
      leaveTime = Some(now().plus(10000)))

    Await.result(storage.clients.get("client1")) shouldBe None
    Await.result(storage.clients.get("client2")) shouldBe None

    Await.result(storage.clients.replace(client1)) shouldBe false
    Await.result(storage.clients.replace(client2)) shouldBe false

    Await.result(storage.clients.create(client1)) shouldBe true
    Await.result(storage.clients.create(client2)) shouldBe true
    Await.result(storage.clients.create(client1)) shouldBe false

    Await.result(storage.clients.get("client1")) shouldBe Some(client1)
    Await.result(storage.clients.get("client2")) shouldBe Some(client2)

    Await.result(storage.clients.list()) shouldBe Seq(client2, client1)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(true)))) shouldBe Seq(client2)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(false)))) shouldBe Seq(client1)

    val newClient1 = client1.copy(leaveTime = Some(now()))
    Await.result(storage.clients.replace(newClient1)) shouldBe true
    Await.result(storage.clients.get("client1")) shouldBe Some(newClient1)
    Await.result(storage.clients.get("client2")) shouldBe Some(client2)

    Await.result(storage.clients.list()) shouldBe Seq(client2, newClient1)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(true)))) shouldBe Seq(client2, newClient1)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(false)))) shouldBe Seq()
  }

  it should "manage campaigns" in {
    val storage = createStorage

    val campaign1 = Campaign(
      name = "campaign1",
      createTime = now(),
      vocabulary = Vocabulary(),
      displayName = None,
      email = Seq.empty,
      startTime = None,
      endTime = None,
      collectRaw = true,
      collectEncrypted = false,
      delay = 0,
      graceDelay = 0,
      groupSize = 0,
      samplingRate = None)
    val campaign2 = Campaign(
      name = "campaign2",
      createTime = now().plus(1000),
      vocabulary = Vocabulary(queries = Seq(VocabularyQuery(exact = Some("foo")), VocabularyQuery(exact = Some("bar")), VocabularyQuery(terms = Some(Seq("a, b"))))),
      displayName = Some("second campaign"),
      email = Seq("v@ucl.ac.uk", "p@ucl.ac.ul"),
      startTime = Some(now()),
      endTime = Some(now().plus(3600 * 1000 * 24 * 5)),
      collectRaw = false,
      collectEncrypted = true,
      delay = 1,
      graceDelay = 2,
      groupSize = 5,
      samplingRate = Some(.5))

    Await.result(storage.campaigns.get("campaign1")) shouldBe None
    Await.result(storage.campaigns.get("campaign2")) shouldBe None

    Await.result(storage.campaigns.replace(campaign1)) shouldBe false
    Await.result(storage.campaigns.replace(campaign2)) shouldBe false

    Await.result(storage.campaigns.create(campaign1)) shouldBe true
    Await.result(storage.campaigns.create(campaign2)) shouldBe true
    Await.result(storage.campaigns.create(campaign1)) shouldBe false

    Await.result(storage.campaigns.get("campaign1")) shouldBe Some(campaign1)
    Await.result(storage.campaigns.get("campaign2")) shouldBe Some(campaign2)

    Await.result(storage.campaigns.batchGet(Seq("campaign1", "campaign2"))) shouldBe Seq(Some(campaign1), Some(campaign2))

    Await.result(storage.campaigns.list()) shouldBe Seq(campaign2, campaign1)
    Await.result(storage.campaigns.list(CampaignQuery(isActive = Some(true)))) shouldBe Seq(campaign2)
    Await.result(storage.campaigns.list(CampaignQuery(isActive = Some(false)))) shouldBe Seq(campaign1)

    val newCampaign1 = campaign1.copy(startTime = Some(now().minus(5000)))
    Await.result(storage.campaigns.replace(newCampaign1)) shouldBe true
    Await.result(storage.campaigns.get("campaign1")) shouldBe Some(newCampaign1)
    Await.result(storage.campaigns.get("campaign2")) shouldBe Some(campaign2)

    Await.result(storage.campaigns.list()) shouldBe Seq(campaign2, newCampaign1)
    Await.result(storage.campaigns.list(CampaignQuery(isActive = Some(true)))) shouldBe Seq(campaign2, newCampaign1)
    Await.result(storage.campaigns.list(CampaignQuery(isActive = Some(false)))) shouldBe Seq()
  }

  private def now(): Instant = DateTime.now().withMillisOfSecond(0).toInstant
}
