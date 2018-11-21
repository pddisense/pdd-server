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

import com.twitter.util.Future
import org.joda.time.Instant

/**
 * A strategy responsible for assigning clients into groups.
 */
trait GroupStrategy {
  def apply(clientNames: Iterable[String], day: Int, attrs: GroupStrategy.Attrs): Future[Iterable[GroupStrategy.Group]]
}

object GroupStrategy {

  case class Attrs(groupSize: Int, startTime: Instant, delay: Int, graceDelay: Int)

  case class Group(id: Int, clientNames: Iterable[String])

  def assign(clientNames: Iterable[String], attrs: GroupStrategy.Attrs): Seq[Group] = {
    clientNames.grouped(attrs.groupSize).zipWithIndex.map { case (groupClientNames, idx) =>
      GroupStrategy.Group(idx, groupClientNames)
    }.toSeq
  }

}
