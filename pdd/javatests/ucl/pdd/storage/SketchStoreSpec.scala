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

abstract class SketchStoreSpec extends StoreSpec {
  it should "manage sketches" in {
    val sketch1 = Sketch(
      name = "sketch1",
      clientName = "client1",
      campaignName = "campaign1",
      group = 0,
      day = 0,
      publicKey = "foo-key==",
      submitTime = Some(now()),
      encryptedValues = Some(Seq("1", "0", "0")),
      rawValues = Some(Seq(1, 1, 0)))
    val sketch2 = Sketch(
      name = "sketch2",
      clientName = "client1",
      campaignName = "campaign1",
      group = 0,
      day = 1,
      publicKey = "foo-key==",
      submitTime = None,
      encryptedValues = None,
      rawValues = None)
    val sketch3 = Sketch(
      name = "sketch3",
      clientName = "client2",
      campaignName = "campaign1",
      group = 0,
      day = 0,
      publicKey = "bar-key==",
      submitTime = None,
      encryptedValues = None,
      rawValues = None)
    val sketch4 = Sketch(
      name = "sketch4",
      clientName = "client2",
      campaignName = "campaign2",
      group = 0,
      day = 0,
      publicKey = "bar-key==",
      submitTime = None,
      encryptedValues = None,
      rawValues = None)

    Await.result(storage.sketches.list()) should have size 0
    Await.result(storage.sketches.replace(sketch1)) shouldBe false
    Await.result(storage.sketches.delete("sketch1")) shouldBe false

    Await.result(storage.sketches.create(sketch1)) shouldBe true
    Await.result(storage.sketches.create(sketch2)) shouldBe true
    Await.result(storage.sketches.create(sketch3)) shouldBe true
    Await.result(storage.sketches.create(sketch4)) shouldBe true
    Await.result(storage.sketches.create(sketch1)) shouldBe false

    Await.result(storage.sketches.list(SketchQuery(campaignName = Some("campaign1")))) should contain theSameElementsAs Seq(sketch1, sketch2, sketch3)
    Await.result(storage.sketches.list(SketchQuery(clientName = Some("client1")))) should contain theSameElementsAs Seq(sketch1, sketch2)
    Await.result(storage.sketches.list(SketchQuery(isSubmitted = Some(true)))) should contain theSameElementsAs Seq(sketch1)
    Await.result(storage.sketches.list(SketchQuery(isSubmitted = Some(false)))) should contain theSameElementsAs Seq(sketch2, sketch3, sketch4)

    val newSketch2 = sketch2.copy(submitTime = Some(now()), encryptedValues = Some(Seq("0", "10", "2")), rawValues = Some(Seq(1, 10, 3)))
    Await.result(storage.sketches.replace(newSketch2)) shouldBe true
    Await.result(storage.sketches.list(SketchQuery(clientName = Some("client1")))) should contain theSameElementsAs Seq(sketch1, newSketch2)
    Await.result(storage.sketches.list(SketchQuery(isSubmitted = Some(true)))) should contain theSameElementsAs Seq(sketch1, newSketch2)
    Await.result(storage.sketches.list(SketchQuery(isSubmitted = Some(false)))) should contain theSameElementsAs Seq(sketch3, sketch4)

    Await.result(storage.sketches.delete("sketch1")) shouldBe true
    Await.result(storage.sketches.list(SketchQuery(clientName = Some("client1")))) should contain theSameElementsAs Seq(newSketch2)
  }
}
