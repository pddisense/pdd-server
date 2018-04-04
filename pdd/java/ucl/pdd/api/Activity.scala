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
 * Record the activity of a client. It is later used to create "optimal" groups when dealing with
 * encrypted sketches collection. An activity is recorded when a client sends a ping request
 * to the server. There might be some rate-limiting built on top of it.
 *
 * @param clientName  Client unique identifier.
 * @param time        Time of the interaction.
 * @param countryCode Code of the country the client is currently located in.
 */
case class Activity(
  clientName: String,
  time: Instant,
  countryCode: Option[String])
