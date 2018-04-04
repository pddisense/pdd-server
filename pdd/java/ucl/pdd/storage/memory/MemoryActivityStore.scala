/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
