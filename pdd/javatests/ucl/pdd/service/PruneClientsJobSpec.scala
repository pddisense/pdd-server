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

package ucl.pdd.service

import com.twitter.conversions.time._
import com.twitter.util.{Await, Future}
import org.joda.time.{Duration, Instant}
import org.scalatest.BeforeAndAfterEach
import ucl.pdd.domain.{Activity, Client}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.testing.UnitSpec

/**
 * Unit tests for [[PruneClientsJob]].
 */
class PruneClientsJobSpec extends UnitSpec with BeforeAndAfterEach {
  behavior of "PruneClientsJob"

  private var job: PruneClientsJob = _
  private var storage: Storage = _

  override def beforeEach(): Unit = {
    storage = MemoryStorage.empty
    Await.ready(storage.startUp())
    job = new PruneClientsJob(storage, 3.days)
  }

  it should "prune clients inactive since at least 3 days" in {
    val now = Instant.now()
    Await.ready(Future.join(
      storage.clients.create(Client("client1", now, "key1", "chrome")),
      storage.clients.create(Client("client2", now, "key2", "chrome")),
      storage.clients.create(Client("client3", now, "key3", "chrome"))
    ))
    Await.ready(Future.join(Seq(
      storage.activity.create(Activity("client1", now.minus(Duration.standardDays(4)), None, None, None)),
      storage.activity.create(Activity("client1", now.minus(Duration.standardDays(5)), None, None, None)),
      storage.activity.create(Activity("client2", now.minus(Duration.standardDays(4)), None, None, None)),
      storage.activity.create(Activity("client2", now.minus(Duration.standardDays(5)), None, None, None)),
      storage.activity.create(Activity("client2", now.minus(Duration.standardDays(2)), None, None, None)),
      storage.activity.create(Activity("client2", now.minus(Duration.standardDays(2)), None, None, None)),
      storage.activity.create(Activity("client3", now.minus(Duration.standardDays(2)), None, None, None))
    )))
    job.execute(now)
    Await.result(storage.clients.list()).map(_.name) should contain theSameElementsAs Seq("client2", "client3")
  }
}
