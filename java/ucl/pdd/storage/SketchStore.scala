/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Sketch

trait SketchStore {
  def save(sketch: Sketch): Future[Unit]

  def delete(name: String): Future[Unit]

  def get(name: String): Future[Option[Sketch]]

  def list(query: SketchQuery = SketchQuery()): Future[Seq[Sketch]]
}

case class SketchQuery(
  clientName: Option[String] = None,
  campaignName: Option[String] = None,
  group: Option[Int] = None,
  day: Option[Int] = None,
  isSubmitted: Option[Boolean] = None) {

  def matches(sketch: Sketch): Boolean = {
    clientName.forall(sketch.clientName == _) &&
      campaignName.forall(sketch.campaignName == _) &&
      group.forall(sketch.group == _) &&
      day.forall(sketch.day == _) &&
      isSubmitted.forall(sketch.isSubmitted == _)
  }
}
