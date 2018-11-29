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

package ucl.pdd.storage.install

import com.google.inject.Module
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.inject.{CreateTwitterInjector, TwitterModule}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.storage.mysql.MysqlStorage
import ucl.testing.UnitSpec

/**
 * Unit tests for [[StorageModule]].
 */
class StorageModuleSpec extends UnitSpec with CreateTwitterInjector {
  behavior of "StorageModule"

  override protected def modules: Seq[Module] = Seq(StorageModule, StatsModule)

  it should "provide a memory storage" in {
    val injector = createInjector()
    injector.instance[Storage] shouldBe a[MemoryStorage]
  }

  it should "provide a zookeeper storage" in {
    val injector = createInjector("-mysql_server", "127.0.0.1")
    injector.instance[Storage] shouldBe a[MysqlStorage]
  }

  private object StatsModule extends TwitterModule {
    override protected def configure(): Unit = {
      bind[StatsReceiver].to[NullStatsReceiver]
    }
  }
}
