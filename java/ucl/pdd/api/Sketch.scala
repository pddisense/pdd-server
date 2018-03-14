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
