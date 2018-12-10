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
import ucl.pdd.domain.Activity

/**
 * Common unit tests for implementations of [[ActivityStore]].
 */
abstract class ActivityStoreSpec extends StoreSpec {
  it should "create and retrieve activity" in {
    Await.result(storage.activity.list()) shouldBe Seq.empty

    val t = now()
    val activity = Seq(
      Activity("client1", t, None, Some("1.2.0"), Some("Europe/London")),
      Activity("client1", t.plus(5000), None, Some("1.2.1"), Some("Europe/London")),
      Activity("client1", t.plus(10000), Some("UK"), Some("1.3.0"), Some("Europe/London")),
      Activity("client2", t.plus(6000), Some("FR"), Some("1.3.0"), Some("Europe/Paris")),
      Activity("client2", t.plus(12000), Some("UK"), Some("1.3.1"), Some("Europe/London")))
    activity.foreach(act => Await.result(storage.activity.create(act)))

    Await.result(storage.activity.list()) should contain theSameElementsAs activity

    Await.result(storage.activity.list(ActivityStore.Query(clientName = Some("client1")))) should contain theSameElementsAs activity.take(3)
    Await.result(storage.activity.list(ActivityStore.Query(countryCode = Some("UK")))) should contain theSameElementsAs Seq(activity(2), activity(4))
    Await.result(storage.activity.list(ActivityStore.Query(startTime = Some(t.plus(1000)), endTime = Some(t.plus(11000))))) should contain theSameElementsAs Seq(
      activity(1),
      activity(2),
      activity(3))
  }

  it should "delete activity" in {
    Await.result(storage.activity.list()) shouldBe Seq.empty

    val t = now()
    val activity = Seq(
      Activity("client1", t, None, Some("1.2.0"), Some("Europe/London")),
      Activity("client1", t.plus(5000), None, Some("1.2.1"), Some("Europe/London")),
      Activity("client1", t.plus(10000), Some("UK"), Some("1.3.0"), Some("Europe/London")),
      Activity("client2", t.plus(6000), Some("FR"), Some("1.3.0"), Some("Europe/Paris")),
      Activity("client2", t.plus(12000), Some("UK"), Some("1.3.1"), Some("Europe/London")))
    activity.foreach(act => Await.result(storage.activity.create(act)))

    Await.result(storage.activity.delete(ActivityStore.Query(clientName = Some("client3")))) shouldBe 0

    Await.result(storage.activity.delete(ActivityStore.Query(clientName = Some("client1")))) shouldBe 3
    Await.result(storage.activity.list()) should contain theSameElementsAs activity.drop(3)

    Await.result(storage.activity.delete(ActivityStore.Query(startTime = Some(now.plus(10000))))) shouldBe 1
    Await.result(storage.activity.list()) should contain theSameElementsAs Seq(activity(3))
  }
}
