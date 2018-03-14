/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
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
