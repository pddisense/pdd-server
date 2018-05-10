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

package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentLinkedQueue

import com.twitter.util.Future
import ucl.pdd.api.Activity
import ucl.pdd.storage.ActivityStore

import scala.collection.JavaConverters._

private[memory] final class MemoryActivityStore extends ActivityStore {
  private[this] val index = new ConcurrentLinkedQueue[Activity]

  override def create(activity: Activity): Future[Unit] = Future {
    index.add(activity)
  }

  override def list(query: ActivityStore.Query): Future[Seq[Activity]] = Future {
    index.iterator.asScala.filter(matches(query, _)).toSeq
  }

  override def delete(query: ActivityStore.Query): Future[Int] = Future {
    val it = index.iterator
    var result = 0
    while (it.hasNext) {
      if (matches(query, it.next)) {
        it.remove()
        result += 1
      }
    }
    result
  }

  private def matches(query: ActivityStore.Query, activity: Activity): Boolean = {
    query.clientName.forall(_ == activity.clientName) &&
      query.countryCode.forall(activity.countryCode.contains) &&
      query.startTime.forall(activity.time.isAfter) &&
      query.endTime.forall(activity.time.isBefore)
  }
}
