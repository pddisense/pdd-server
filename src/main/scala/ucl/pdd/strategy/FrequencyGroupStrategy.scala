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

package ucl.pdd.strategy

import com.google.inject.{Inject, Singleton}
import com.twitter.util.Future
import org.joda.time.Duration
import ucl.pdd.domain.Campaign
import ucl.pdd.storage.{ActivityStore, Storage}

@Singleton
final class FrequencyGroupStrategy @Inject()(storage: Storage)
  extends GroupStrategy {

  override def apply(clientNames: Iterable[String], day: Int, attrs: GroupStrategy.Attrs): Future[Iterable[GroupStrategy.Group]] = {
    val endTime = Campaign.absoluteInstant(attrs.startTime, day).minus(Duration.standardDays(1))
    val startTime = endTime.minus(Duration.standardDays(60))
    val threshold = 1d / attrs.delay

    val fs = clientNames.toSeq.map { clientName =>
      storage.activity
        .list(ActivityStore.Query(
          clientName = Some(clientName),
          startTime = Some(startTime),
          endTime = Some(endTime.toInstant)))
        .map { activity =>
          val activeDays = activity.map(d => Campaign.relativeDay(attrs.startTime, d.time))
            .toSet
            .size
          if ((activeDays.toDouble / math.min(day - 1, 30)) > threshold) {
            Set(clientName)
          } else {
            Set.empty[String]
          }
        }
    }

    Future.collect(fs).map(clientNames => GroupStrategy.assign(clientNames.flatten, attrs))
  }
}
