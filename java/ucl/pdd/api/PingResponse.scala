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
 * @param name             Name of the sketch to submit.
 * @param startTime        Start time of collection period, i.e., collected searches should be made
 *                         after this time.
 * @param endTime          End time of collection period, i.e., collected searches should be made
 *                         before this time.
 * @param vocabulary       A delta update appending new queries to the local version of the
 *                         vocabulary the client is aware of. It is empty if the client is up-to-date.
 * @param publicKeys       List of public keys of the other members of the group.
 * @param collectRaw       Whether to send raw counts.
 * @param collectEncrypted Whether to send encrypted counts.
 */
case class SubmitSketchCommand(
  name: String,
  startTime: Instant,
  endTime: Instant,
  vocabulary: Option[Vocabulary],
  publicKeys: Seq[ClientKey],
  collectRaw: Boolean,
  collectEncrypted: Boolean)

case class ClientKey(publicKey: String, isOwn: Boolean)
