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

package ucl.pdd.domain

import org.joda.time.Instant

/**
 * A response that sent back to the client when the latter pings the server.
 *
 * @param submit       A list of commands the client should execute.
 * @param nextPingTime Next time at which the client should ping the server.
 * @see [[ucl.pdd.service.PingService]]
 */
case class PingResponse(submit: Seq[PingResponse.Command], nextPingTime: Option[Instant] = None)

object PingResponse {

  /**
   * A request to submit a sketch with raw and/or encrypted counts.
   *
   * @param sketchName       Name of the sketch to submit.
   * @param startTime        Start time of collection period, i.e., collected searches should be made
   *                         after this time.
   * @param endTime          End time of collection period, i.e., collected searches should be made
   *                         until this time.
   * @param vocabulary       Vocabulary monitored by the associated campaign. For now the whole
   *                         vocabulary is sent with every command.
   * @param publicKeys       List of public keys of the other members of the group. The order matters
   *                         (it should be the same for all commands of a given group) and it has to
   *                         include the key of the client receiving this command.
   * @param collectRaw       Whether to send raw counts.
   * @param collectEncrypted Whether to send encrypted counts.
   * @param round            Round. Practically, it is the same thing than the day number.
   */
  case class Command(
    sketchName: String,
    startTime: Instant,
    endTime: Instant,
    vocabulary: Vocabulary,
    publicKeys: Seq[String],
    collectRaw: Boolean,
    collectEncrypted: Boolean,
    round: Int)

}
