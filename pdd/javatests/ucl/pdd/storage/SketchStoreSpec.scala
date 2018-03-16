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
import ucl.pdd.api.Sketch

/**
 * Common unit tests for implementations of [[SketchStore]].
 */
abstract class SketchStoreSpec extends StoreSpec {
  private[this] val sketches = Seq(
    Sketch(
      name = "sketch1",
      clientName = "client1",
      campaignName = "campaign1",
      group = 0,
      day = 0,
      publicKey = "foo-key==",
      submitTime = Some(now()),
      encryptedValues = Some(Seq("1", "0", "0")),
      rawValues = Some(Seq(1, 1, 0))),
    Sketch(
      name = "sketch2",
      clientName = "client1",
      campaignName = "campaign1",
      group = 0,
      day = 1,
      publicKey = "foo-key==",
      submitTime = None,
      encryptedValues = None,
      rawValues = None),
    Sketch(
      name = "sketch3",
      clientName = "client2",
      campaignName = "campaign1",
      group = 0,
      day = 0,
      publicKey = "bar-key==",
      submitTime = None,
      encryptedValues = None,
      rawValues = None),
    Sketch(
      name = "sketch4",
      clientName = "client2",
      campaignName = "campaign2",
      group = 0,
      day = 0,
      publicKey = "bar-key==",
      submitTime = None,
      encryptedValues = None,
      rawValues = None))

  it should "create and retrieve sketches" in {
    Await.result(storage.sketches.list()) should have size 0

    sketches.foreach(sketch => Await.result(storage.sketches.create(sketch)) shouldBe true)
    Await.result(storage.sketches.create(sketches.head)) shouldBe false

    Await.result(storage.sketches.list(SketchStore.Query(campaignName = Some("campaign1")))) should contain theSameElementsAs Seq(sketches(0), sketches(1), sketches(2))
    Await.result(storage.sketches.list(SketchStore.Query(clientName = Some("client1")))) should contain theSameElementsAs Seq(sketches(0), sketches(1))
    Await.result(storage.sketches.list(SketchStore.Query(isSubmitted = Some(true)))) should contain theSameElementsAs Seq(sketches(0))
    Await.result(storage.sketches.list(SketchStore.Query(isSubmitted = Some(false)))) should contain theSameElementsAs Seq(sketches(1), sketches(2), sketches(3))
  }

  it should "replace sketches" in {
    Await.result(storage.sketches.replace(sketches(0))) shouldBe false

    Await.result(storage.sketches.create(sketches(0)))
    Await.result(storage.sketches.create(sketches(1)))

    val newSketch2 = sketches(1).copy(submitTime = Some(now()), encryptedValues = Some(Seq("0", "10", "2")), rawValues = Some(Seq(1, 10, 3)))
    Await.result(storage.sketches.replace(newSketch2)) shouldBe true
    Await.result(storage.sketches.get("sketch1")) shouldBe Some(sketches(0))
    Await.result(storage.sketches.get("sketch2")) shouldBe Some(newSketch2)
    Await.result(storage.sketches.list()) should contain theSameElementsAs Seq(sketches(0), newSketch2)
  }

  it should "delete sketches" in {
    Await.result(storage.sketches.delete("sketch1")) shouldBe false

    Await.result(storage.sketches.create(sketches(0)))
    Await.result(storage.sketches.create(sketches(1)))

    Await.result(storage.sketches.delete("sketch1")) shouldBe true
    Await.result(storage.sketches.get("sketch1")) shouldBe None
    Await.result(storage.sketches.get("sketch2")) shouldBe Some(sketches(1))
    Await.result(storage.sketches.list()) should contain theSameElementsAs Seq(sketches(1))
  }
}
