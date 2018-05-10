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

package ucl.pdd.storage.mysql

import java.util.UUID

import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{Client => MysqlClient}
import com.twitter.util.Await
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.storage.{Storage, StoreSpec}

private[mysql] trait MysqlStoreSpec extends StoreSpec with BeforeAndAfterEach {
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
      password = null,
      database = base,
      server = "0.0.0.0:3306")
    new MysqlStorage(client)
  }
}
