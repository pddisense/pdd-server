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

import com.google.inject.{Provider, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import com.twitter.util.Await
import ucl.pdd.storage.Storage
import ucl.pdd.storage.memory.MemoryStorage
import ucl.pdd.storage.mysql.{MysqlClientFactory, MysqlStorage}

object StorageModule extends TwitterModule {
  private[this] val typeFlag = flag[String](s"storage.type", "memory", "Storage type ('memory', or 'mysql')")

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
        password = mysqlPassFlag.get.orNull,
        database = mysqlBaseFlag(),
        server = mysqlServerFlag())
      new MysqlStorage(client)
    }
  }

  override def singletonStartup(injector: Injector): Unit = {
    Await.ready(injector.instance[Storage].startUp())
  }

  override def singletonShutdown(injector: Injector): Unit = {
    Await.ready(injector.instance[Storage].shutDown())
  }
}
