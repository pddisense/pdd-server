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

package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.ServerError
import com.twitter.util.Monitor

/**
 * Exception monitor for the MySQL client. This feels a bit like a hack, but it prevents
 * legitimate exceptions (that we handle properly with rescue/handle) to be forwarded to
 * the default monitor. Even in the latest case, it would not have much of an impact, but
 * it makes the logs cleaner. The caveat is that the whitelisted error codes are
 * permanently and silently ignored (we unfortunately cannot install a monitor on a
 * per-query basis).
 */
private[mysql] object MysqlMonitor extends Monitor {
  private[this] val whitelist = Set(
    1062 /* Duplicate key */,
    1146 /* Table does not exist */)

  override def handle(exc: Throwable): Boolean = {
    exc match {
      case s: ServerError => whitelist.contains(s.code)
      case _ => false
    }
  }
}
