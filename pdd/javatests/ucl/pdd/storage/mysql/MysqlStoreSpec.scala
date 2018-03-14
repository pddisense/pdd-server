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
      pass = null,
      base = base,
      server = "0.0.0.0:3306")
    new MysqlStorage(client)
  }
}
