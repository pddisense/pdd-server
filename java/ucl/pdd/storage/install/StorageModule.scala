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
      val clientFactory = new MysqlClientFactory(
        user = mysqlUserFlag(),
        pass = mysqlPassFlag(),
        base = mysqlBaseFlag(),
        server = mysqlServerFlag())
      new MysqlStorage(clientFactory())
    }
  }

  override def singletonStartup(injector: Injector): Unit = {
    injector.instance[Storage].startAsync().awaitRunning()
  }

  override def singletonShutdown(injector: Injector): Unit = {
    injector.instance[Storage].stopAsync().awaitTerminated()
  }
}
