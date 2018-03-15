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

import com.google.inject.{Provider, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.storage.mysql.{MysqlClientFactory, MysqlStorage}

object StorageModule extends TwitterModule {
  private[this] val typeFlag = flag[String](s"storage.type", "memory", "Storage type ('memory', 'mysql' or 'zk')")

  // MySQL options.
  private[this] val mysqlServerFlag = flag("storage.mysql.server", "127.0.0.1:3306", "Address to MySQL server")
  private[this] val mysqlUserFlag = flag("storage.mysql.user", "root", "MySQL username")
  private[this] val mysqlPassFlag = flag[String]("storage.mysql.pass", "MySQL password")
  private[this] val mysqlBaseFlag = flag("storage.mysql.database", "pdd", "MySQL database")

  override def configure(): Unit = {
    typeFlag() match {
      case "memory" => bind[Storage].toProvider[MemoryStorageProvider].in[Singleton]
      case "mysql" => bind[Storage].toProvider[MysqlStorageProvider].in[Singleton]
      case invalid => throw new IllegalArgumentException(s"Invalid storage type: $invalid")
    }
  }

  private class MemoryStorageProvider extends Provider[Storage] {
    override def get(): Storage = new MemoryStorage
  }

  private class MysqlStorageProvider extends Provider[Storage] {
    override def get(): Storage = {
      val client = MysqlClientFactory(
        user = mysqlUserFlag(),
        pass = mysqlPassFlag.get.orNull,
        base = mysqlBaseFlag(),
        server = mysqlServerFlag())
      new MysqlStorage(client)
    }
  }

  override def singletonStartup(injector: Injector): Unit = {
    injector.instance[Storage].startAsync().awaitRunning()
  }

  override def singletonShutdown(injector: Injector): Unit = {
    injector.instance[Storage].stopAsync().awaitTerminated()
  }
}
