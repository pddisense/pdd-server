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
   * @param submitted    Return only sketches that have (or not) been submitted.
   */
  case class Query(
    clientName: Option[String] = None,
    campaignName: Option[String] = None,
    group: Option[Int] = None,
    day: Option[Int] = None,
    submitted: Option[Boolean] = None)

}
