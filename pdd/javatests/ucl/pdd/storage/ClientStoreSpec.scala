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

abstract class ClientStoreSpec extends StoreSpec {
  it should "manage clients" in {
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
    Await.result(storage.clients.list()) shouldBe Seq.empty
    Await.result(storage.clients.replace(client1)) shouldBe false

    Await.result(storage.clients.create(client1)) shouldBe true
    Await.result(storage.clients.create(client2)) shouldBe true
    Await.result(storage.clients.create(client1)) shouldBe false

    Await.result(storage.clients.get("client1")) shouldBe Some(client1)
    Await.result(storage.clients.get("client2")) shouldBe Some(client2)

    Await.result(storage.clients.list()) shouldBe Seq(client2, client1)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(true)))) should contain theSameElementsInOrderAs Seq(client2)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(false)))) should contain theSameElementsInOrderAs Seq(client1)

    val newClient1 = client1.copy(leaveTime = Some(now()))
    Await.result(storage.clients.replace(newClient1)) shouldBe true
    Await.result(storage.clients.get("client1")) shouldBe Some(newClient1)
    Await.result(storage.clients.get("client2")) shouldBe Some(client2)

    Await.result(storage.clients.list()) shouldBe Seq(client2, newClient1)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(true)))) should contain theSameElementsInOrderAs Seq(client2, newClient1)
    Await.result(storage.clients.list(ClientQuery(hasLeft = Some(false)))) should have size 0
  }
}
