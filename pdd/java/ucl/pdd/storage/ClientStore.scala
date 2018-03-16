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
  def create(client: Client): Future[Boolean]

  def replace(client: Client): Future[Boolean]

  def list(query: ClientStore.Query = ClientStore.Query()): Future[Seq[Client]]

  def get(name: String): Future[Option[Client]]
}

object ClientStore {
  case class Query(hasLeft: Option[Boolean] = None)
}
