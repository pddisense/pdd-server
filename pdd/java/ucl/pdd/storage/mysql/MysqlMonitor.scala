package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.ServerError
import com.twitter.util.Monitor

private[mysql] object MysqlMonitor extends Monitor {
  private[this] val whitelist = Set(
    1062, /* Duplicate key */
    1146 /* Table does not exist */)

  override def handle(exc: Throwable): Boolean = {
    exc match {
      case s: ServerError => whitelist.contains(s.code)
      case _ => false
    }
  }
}
