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

package ucl.pdd.storage

import com.twitter.util.Await
import ucl.pdd.api.Client

/**
 * Common unit tests for implementations of [[ClientStore]].
 */
abstract class ClientStoreSpec extends StoreSpec {
  private[this] val clients = Seq(
    Client(
      name = "client1",
      createTime = now(),
      browser = "scalatest",
      publicKey = "foobar=="),
    Client(
      name = "client2",
      createTime = now().plus(1000),
      browser = "scalatest",
      publicKey = "foobar==",
      externalName = Some("foo")))

  it should "create and retrieve clients" in {
    Await.result(storage.clients.get("client1")) shouldBe None
    Await.result(storage.clients.list()) shouldBe Seq.empty

    clients.foreach(client => Await.result(storage.clients.create(client)) shouldBe true)
    Await.result(storage.clients.create(clients.head)) shouldBe false

    Await.result(storage.clients.get("client1")) shouldBe Some(clients(0))
    Await.result(storage.clients.get("client2")) shouldBe Some(clients(1))
    Await.result(storage.clients.list()) shouldBe Seq(clients(1), clients(0))
  }

  it should "replace clients" in {
    Await.result(storage.clients.replace(clients(0))) shouldBe false

    clients.foreach(client => Await.result(storage.clients.create(client)) shouldBe true)

    val newClient1 = clients(0).copy(browser = "fakebrowser")
    Await.result(storage.clients.replace(newClient1)) shouldBe true
    Await.result(storage.clients.get("client1")) shouldBe Some(newClient1)
    Await.result(storage.clients.get("client2")) shouldBe Some(clients(1))
  }

  it should "delete clients" in {
    Await.result(storage.clients.delete("client1")) shouldBe false

    clients.foreach(client => Await.result(storage.clients.create(client)) shouldBe true)

    Await.result(storage.clients.delete("client1")) shouldBe true
    Await.result(storage.clients.get("client1")) shouldBe None
    Await.result(storage.clients.get("client2")) shouldBe Some(clients(1))
  }
}
