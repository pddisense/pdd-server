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

package ucl.pdd.storage

import com.twitter.util.Await
import ucl.pdd.domain.Client

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

  it should "count clients" in {
    Await.result(storage.clients.count()) shouldBe 0

    clients.foreach(client => Await.result(storage.clients.create(client)) shouldBe true)
    Await.result(storage.clients.count()) shouldBe 2
  }

  it should "replace clients" in {
    Await.result(storage.clients.replace(clients(0))) shouldBe false

    clients.foreach(client => Await.result(storage.clients.create(client)) shouldBe true)

    val newClient1 = clients(0).copy(browser = "fakebrowser")
    println(clients(0))
    println(newClient1)
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

  it should "retrieve clients in batch" in {
    clients.foreach(client => Await.result(storage.clients.create(client)) shouldBe true)

    Await.result(storage.clients.multiGet(Seq("client42", "client2", "client1"))) should contain theSameElementsInOrderAs Seq(
      None,
      Some(clients(1)),
      Some(clients(0)))
  }
}
