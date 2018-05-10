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

package ucl.pdd.api

/**
 * A sketch is an individual request to collect searches during a given time period
 * concerning a given client. Sketches are created at the same moment than aggregations, and
 * (hopefully) filled when clients send their sketches. Sketches should expected to be garbage
 * collected once an aggregation is expired.
 *
 * @param name         Sketch unique identifier.
 * @param clientName   The name of the client this sketch is about.
 * @param campaignName The name of the campaign this sketch is about.
 * @param group
 * @param day
 * @param publicKey
 * @param submitted    Whether this sketch has been submitted.
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
  submitted: Boolean,
  encryptedValues: Option[Seq[String]] = None,
  rawValues: Option[Seq[Long]] = None)
