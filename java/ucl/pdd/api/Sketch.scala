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
 * A sketch is an individual request to collect searches during a given time period
 * concerning a given client. Sketches are created at the same moment than aggregations, and
 * (hopefully) filled when clients send their sketches. Sketches should expected to be garbage
 * collected once an aggregation is expired.
 *
 * @param name
 * @param clientName The name of the client this sketch is about.
 * @param campaignName
 * @param group
 * @param day
 * @param publicKey
 * @param submitTime Time at which the sketch was submitted.
 * @param encryptedValues
 * @param rawValues
 */
case class Sketch(
  name: String,
  clientName: String,
  campaignName: String,
  group: Int,
  day: Int,
  publicKey: String,
  submitTime: Option[Instant] = None,
  encryptedValues: Option[Seq[String]] = None,
  rawValues: Option[Seq[Long]] = None) {

  def isSubmitted: Boolean = submitTime.isDefined
}
