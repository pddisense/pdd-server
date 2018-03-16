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
    val injector = createInjector("-storage.type", "memory")
    injector.instance[Storage] shouldBe a[MemoryStorage]
  }

  it should "provide a zookeeper storage" in {
    val injector = createInjector("-storage.type", "mysql")
    injector.instance[Storage] shouldBe a[MysqlStorage]
  }

  private object StatsModule extends TwitterModule {
    override protected def configure(): Unit = {
      bind[StatsReceiver].to[NullStatsReceiver]
    }
  }
}
