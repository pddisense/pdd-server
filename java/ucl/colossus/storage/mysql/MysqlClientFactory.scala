/*
 * Colossus is framework to build API servers, based on Finagle.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Accio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Accio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Colossus.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.colossus.storage.mysql

import com.twitter.conversions.time._
import com.twitter.finagle.Mysql
import com.twitter.finagle.client.DefaultPool
import com.twitter.finagle.mysql.Client

final class MysqlClientFactory(server: String, user: String, pass: String, base: String) {
  def apply(): Client = {
    Mysql.client
      .withCredentials(user, pass)
      .withDatabase(base)
      .withMonitor(MysqlMonitor)
      .configured(DefaultPool.Param(
        low = 0,
        high = 10,
        idleTime = 5.minutes,
        bufferSize = 0,
        maxWaiters = Int.MaxValue))
      .newRichClient(server)
  }
}
