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

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import ucl.pdd.api.Sketch
import ucl.pdd.storage.SketchStore

import scala.collection.JavaConverters._

private[memory] final class MemorySketchStore extends SketchStore {
  private[this] val index = new ConcurrentHashMap[String, Sketch]().asScala

  override def create(sketch: Sketch): Future[Boolean] = Future {
    index.putIfAbsent(sketch.name, sketch).isEmpty
  }

  override def replace(sketch: Sketch): Future[Boolean] = Future {
    index.replace(sketch.name, sketch).isDefined
  }

  override def delete(name: String): Future[Boolean] = Future {
    index.remove(name).isDefined
  }

  override def list(query: SketchStore.Query = SketchStore.Query()): Future[Seq[Sketch]] = Future {
    index.values.filter(matches(query, _)).toSeq
  }

  override def get(name: String): Future[Option[Sketch]] = Future.value(index.get(name))

  private def matches(query: SketchStore.Query, sketch: Sketch): Boolean = {
    query.clientName.forall(sketch.clientName == _) &&
      query.campaignName.forall(sketch.campaignName == _) &&
      query.group.forall(sketch.group == _) &&
      query.day.forall(sketch.day == _) &&
      query.submitted.forall(sketch.submitted == _)
  }
}
