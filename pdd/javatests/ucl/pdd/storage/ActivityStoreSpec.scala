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
import ucl.pdd.api.Activity

/**
 * Common unit tests for implementations of [[ActivityStore]].
 */
abstract class ActivityStoreSpec extends StoreSpec {
  it should "create and retrieve activity" in {
    Await.result(storage.activity.list()) shouldBe Seq.empty

    val t = now()
    val activity = Seq(
      Activity("client1", t, None),
      Activity("client1", t.plus(5000), None),
      Activity("client1", t.plus(10000), Some("uk")),
      Activity("client2", t.plus(6000), Some("uk")),
      Activity("client2", t.plus(12000), Some("uk")))
    activity.foreach(act => Await.result(storage.activity.create(act)))

    Await.result(storage.activity.list()) should contain theSameElementsAs activity

    Await.result(storage.activity.list(ActivityStore.Query(clientName = Some("client1")))) should contain theSameElementsAs activity.take(3)
    Await.result(storage.activity.list(ActivityStore.Query(countryCode = Some("uk")))) should contain theSameElementsAs Seq(activity(2), activity(3), activity(4))
    Await.result(storage.activity.list(ActivityStore.Query(startTime = Some(now.plus(1000)), endTime = Some(now.plus(11000))))) should contain theSameElementsAs Seq(activity(1), activity(2), activity(3))
  }

  it should "delete activity" in {
    Await.result(storage.activity.list()) shouldBe Seq.empty

    val t = now()
    val activity = Seq(
      Activity("client1", t, None),
      Activity("client1", t.plus(5000), None),
      Activity("client1", t.plus(10000), Some("uk")),
      Activity("client2", t.plus(6000), Some("uk")),
      Activity("client2", t.plus(12000), Some("uk")))
    activity.foreach(act => Await.result(storage.activity.create(act)))

    Await.result(storage.activity.delete(ActivityStore.Query(clientName = Some("client3")))) shouldBe 0

    Await.result(storage.activity.delete(ActivityStore.Query(clientName = Some("client1")))) shouldBe 3
    Await.result(storage.activity.list()) should contain theSameElementsAs activity.drop(3)

    Await.result(storage.activity.delete(ActivityStore.Query(startTime = Some(now.plus(10000))))) shouldBe 1
    Await.result(storage.activity.list()) should contain theSameElementsAs Seq(activity(3))
  }
}
