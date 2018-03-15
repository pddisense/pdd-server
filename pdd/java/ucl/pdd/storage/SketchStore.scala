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

package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Sketch

trait SketchStore {
  def create(sketch: Sketch): Future[Boolean]

  def replace(sketch: Sketch): Future[Boolean]

  def delete(name: String): Future[Boolean]

  def list(query: SketchQuery = SketchQuery()): Future[Seq[Sketch]]

  def get(name: String): Future[Option[Sketch]]
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
