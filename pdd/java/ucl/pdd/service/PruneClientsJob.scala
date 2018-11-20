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

package ucl.pdd.service

import com.google.inject.{Inject, Singleton}
import com.twitter.util.logging.Logging
import com.twitter.util.{Duration, Future}
import org.joda.time.Instant
import ucl.pdd.storage.{ActivityStore, Storage}

@Singleton
final class PruneClientsJob @Inject()(storage: Storage, @PruneThreshold pruneThreshold: Duration)
  extends Logging {
  def execute(fireTime: Instant): Unit = {
    val f1 = storage.clients.list()
    val f2 = storage.activity.list(ActivityStore.Query(startTime = Some(fireTime.minus(pruneThreshold.inMillis))))
    Future.join(f1, f2).flatMap { case (allClients, activeClients) =>
      val inactiveClients = allClients.map(_.name).diff(activeClients.map(_.clientName))
      Future.join(inactiveClients.map(deleteClient))
    }
  }

  private def deleteClient(clientName: String): Future[Unit] = {
    Future.join(
      storage.clients.delete(clientName),
      storage.activity.delete(ActivityStore.Query(clientName = Some(clientName)))
    ).unit
  }
}
