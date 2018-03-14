package ucl.pdd.storage.mysql

import java.util.UUID

import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{Client => MysqlClient}
import com.twitter.util.Await
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.storage.{Storage, StorageSpec}

/**
 * Unit tests for [[MysqlStorage]].
 */
class MysqlStorageSpec extends StorageSpec with BeforeAndAfterEach {
  behavior of "MysqlStorage"

  private[this] var initClient: MysqlClient = _
  private[this] var base: String = _

  override def beforeEach(): Unit = {
    base = "test_" + UUID.randomUUID().getLeastSignificantBits.toHexString
    initClient = Mysql.client.withCredentials("root", null).newRichClient("0.0.0.0:3306")
    Await.result(initClient.query(s"create database $base"))

    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Await.result(initClient.query(s"drop database $base"))
    initClient.close()

    initClient = null
    base = null

    super.afterEach()
  }

  override def createStorage: Storage = {
    val client = MysqlClientFactory(
      user = "root",
      pass = null,
      base = base,
      server = "0.0.0.0:3306")
    new MysqlStorage(client)
  }
}
