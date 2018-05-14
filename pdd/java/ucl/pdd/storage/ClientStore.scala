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
import ucl.pdd.domain.Client

trait ClientStore {
  /**
   * Persist a new client, if no other client with the same name exists.
   *
   * @param client A client to create.
   * @return Whether the client was successfully created.
   */
  def create(client: Client): Future[Boolean]

  /**
   * Replace an existing client with a new one, if such an client with the same name
   * already exists. All fields will be modified according to the values of the new client.
   *
   * @param client A client to update.
   * @return Whether the client was successfully replaced.
   */
  def replace(client: Client): Future[Boolean]

  /**
   * Retrieve a single client by its name, if it exists.
   *
   * @param name A client name.
   */
  def get(name: String): Future[Option[Client]]

  /**
   * Delete a single client by its name, if it exists.
   *
   * @param name A client name.
   */
  def delete(name: String): Future[Boolean]

  /**
   * Retrieve all clients.
   */
  def list(): Future[Seq[Client]]

  /**
   * Count all clients.
   */
  def count(): Future[Int]
}
