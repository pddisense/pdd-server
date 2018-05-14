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

import com.github.nscala_time.time.Imports._
import com.twitter.util.Future
import ucl.pdd.domain.Client
import ucl.pdd.storage.ClientStore

import scala.collection.JavaConverters._

private[memory] final class MemoryClientStore extends ClientStore {
  private[this] val index = new ConcurrentHashMap[String, Client]().asScala

  override def create(client: Client): Future[Boolean] = Future {
    index.putIfAbsent(client.name, client).isEmpty
  }

  override def replace(client: Client): Future[Boolean] = Future {
    index.replace(client.name, client).isDefined
  }

  override def list(): Future[Seq[Client]] = Future {
    index
      .values
      .toSeq
      .sortWith { case (a, b) => a.createTime > b.createTime }
  }

  override def count(): Future[Int] = Future(index.size)

  override def get(name: String): Future[Option[Client]] = Future(index.get(name))

  override def delete(name: String): Future[Boolean] = Future(index.remove(name).isDefined)
}
