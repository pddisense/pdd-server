package ucl.pdd.storage

import com.google.inject.{Provider, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import com.twitter.util.Await
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
