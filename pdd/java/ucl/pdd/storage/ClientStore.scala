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
import ucl.pdd.api.Client

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
