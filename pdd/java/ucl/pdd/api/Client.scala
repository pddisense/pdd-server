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

package ucl.pdd.api

import org.joda.time.Instant

/**
 * A client corresponds to a registered browser extension. We do not track users across multiple
 * browsers, i.e, they would each behave as a different client.
 *
 * @param name         Client name, unique among all clients.
 * @param createTime   Time at which the client was created.
 * @param publicKey    Public key.
 * @param browser      Identifier of the browser used by this client.
 * @param externalName A name voluntarily filled by the user allowing to identify him.
 */
case class Client(
  name: String,
  createTime: Instant,
  publicKey: String,
  browser: String,
  externalName: Option[String] = None)
