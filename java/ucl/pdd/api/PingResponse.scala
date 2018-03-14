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
 *
 * @param submit       A list of sketches that the client should submit.
 * @param nextPingTime Next time at which the client should ping the server.
 */
case class PingResponse(submit: Seq[SubmitSketchCommand], nextPingTime: Option[Instant] = None)

/**
 * A request to submit a sketch with raw and/or encrypted counts.
 *
 * @param sketchName       Name of the sketch to submit.
 * @param startTime        Start time of collection period, i.e., collected searches should be made
 *                         after this time.
 * @param endTime          End time of collection period, i.e., collected searches should be made
 *                         before this time.
 * @param vocabulary       A delta update appending new queries to the local version of the
 *                         vocabulary the client is aware of. It is empty if the client is up-to-date.
 * @param publicKeys       List of public keys of the other members of the group.
 * @param collectRaw       Whether to send raw counts.
 * @param collectEncrypted Whether to send encrypted counts.
 * @param round            Round.
 */
case class SubmitSketchCommand(
  sketchName: String,
  startTime: Instant,
  endTime: Instant,
  vocabulary: Option[Vocabulary],
  publicKeys: Seq[String],
  collectRaw: Boolean,
  collectEncrypted: Boolean,
  round: Int)
