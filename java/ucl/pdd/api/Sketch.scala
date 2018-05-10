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
