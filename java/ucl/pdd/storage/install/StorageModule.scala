/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
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
  private[this] val mysqlUserFlag = flag[String]("storage.mysql.user", "root", "MySQL username")
  private[this] val mysqlPassFlag = flag("storage.mysql.pass", "", "MySQL password")
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
        pass = mysqlPassFlag(),
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
