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
  /**
   * Persist a new sketch, if no other sketch with the same name exists.
   *
   * @param sketch A sketch to create.
   * @return Whether the sketch was successfully created.
   */
  def create(sketch: Sketch): Future[Boolean]

  /**
   * Replace an existing sketch with a new one, if such an sketch with the same name
   * already exists. All fields will be modified according to the values of the new sketch.
   *
   * @param sketch A sketch to update.
   * @return Whether the sketch was successfully replaced.
   */
  def replace(sketch: Sketch): Future[Boolean]

  /**
   * Retrieve a single sketch by its name, if it exists.
   *
   * @param name A sketch name.
   */
  def get(name: String): Future[Option[Sketch]]

  /**
   * Retrieve several sketches according to a query. No specific order is enforced, the order in
   * which sketches are returned may be implementation-dependant.
   *
   * @param query A query to filter sketches.
   */
  def list(query: SketchStore.Query = SketchStore.Query()): Future[Seq[Sketch]]

  /**
   * Delete an existing sketch, if it exists.
   *
   * @param name A sketch name.
   * @return Whether the sketch was successfully deleted.
   */
  def delete(name: String): Future[Boolean]
}

object SketchStore {

  /**
   * A query used to filter sketches.
   *
   * @param clientName   Return only sketches belonging to a given client.
   * @param campaignName Return only sketches belonging to a given campaign.
   * @param group        Return only sketches associated with a given group.
   * @param day          Return only sketches associated with a given day.
   * @param isSubmitted  Return only sketches that have (or not) been submitted.
   */
  case class Query(
    clientName: Option[String] = None,
    campaignName: Option[String] = None,
    group: Option[Int] = None,
    day: Option[Int] = None,
    isSubmitted: Option[Boolean] = None)

}
