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

/**
 * Guice module configuring a storage.
 */
object StorageModule extends TwitterModule {
  // MySQL options.
  private[this] val mysqlServerFlag = flag[String]("mysql_server", "Address to MySQL server")
  private[this] val mysqlUserFlag = flag("mysql_user", "root", "MySQL username")
  private[this] val mysqlPassFlag = flag[String]("mysql_password", "MySQL password")
  private[this] val mysqlBaseFlag = flag("mysql_database", "pdd", "MySQL database")

  override def configure(): Unit = {
    if (mysqlServerFlag.isDefined) {
      bind[Storage].toProvider[MysqlStorageProvider].in[Singleton]
    } else {
      warn("Running with ephemeral in-memory storage.")
      bind[Storage].toProvider[MemoryStorageProvider].in[Singleton]
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
    injector.instance[Storage].shutDown()
  }
}
