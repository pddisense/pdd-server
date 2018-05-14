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
import ucl.pdd.domain.Sketch

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
      submitted = true,
      queriesCount = 2,
      encryptedValues = Some(Seq("1", "0", "0")),
      rawValues = Some(Seq(1, 1, 0))),
    Sketch(
      name = "sketch2",
      clientName = "client1",
      campaignName = "campaign1",
      group = 0,
      day = 1,
      publicKey = "foo-key==",
      submitted = false,
      queriesCount = 3,
      encryptedValues = None,
      rawValues = None),
    Sketch(
      name = "sketch3",
      clientName = "client2",
      campaignName = "campaign1",
      group = 0,
      day = 0,
      publicKey = "bar-key==",
      queriesCount = 2,
      submitted = false,
      encryptedValues = None,
      rawValues = None),
    Sketch(
      name = "sketch4",
      clientName = "client2",
      campaignName = "campaign2",
      group = 0,
      day = 0,
      publicKey = "bar-key==",
      submitted = false,
      queriesCount = 5,
      encryptedValues = None,
      rawValues = None))

  it should "create and retrieve sketches" in {
    Await.result(storage.sketches.list()) should have size 0

    sketches.foreach(sketch => Await.result(storage.sketches.create(sketch)) shouldBe true)
    Await.result(storage.sketches.create(sketches.head)) shouldBe false

    Await.result(storage.sketches.list(SketchStore.Query(campaignName = Some("campaign1")))) should contain theSameElementsAs Seq(sketches(0), sketches(1), sketches(2))
    Await.result(storage.sketches.list(SketchStore.Query(clientName = Some("client1")))) should contain theSameElementsAs Seq(sketches(0), sketches(1))
    Await.result(storage.sketches.list(SketchStore.Query(submitted = Some(true)))) should contain theSameElementsAs Seq(sketches(0))
    Await.result(storage.sketches.list(SketchStore.Query(submitted = Some(false)))) should contain theSameElementsAs Seq(sketches(1), sketches(2), sketches(3))
  }

  it should "replace sketches" in {
    Await.result(storage.sketches.replace(sketches(0))) shouldBe false

    Await.result(storage.sketches.create(sketches(0)))
    Await.result(storage.sketches.create(sketches(1)))

    val newSketch2 = sketches(1).copy(submitted = true, encryptedValues = Some(Seq("0", "10", "2")), rawValues = Some(Seq(1, 10, 3)))
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
