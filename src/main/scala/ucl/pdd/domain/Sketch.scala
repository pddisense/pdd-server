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
 * A sketch is an individual request to collect searches during a given time period
 * concerning a given client. Sketches are created at the same moment than aggregations, and
 * (hopefully) filled later when clients send their data. Sketches may be garbage-collected once
 * an aggregation is expired.
 *
 * The query counts (stored under `encryptedValues` and `rawValues`) are modelled as an array of
 * integers. The first value is always the total number of searches made over the day (this
 * includes all searches, whether they are actively monitored or not). There must then be one value
 * per query in the vocabulary at the time the sketch was generated (i.e., `queriesCount`). It
 * means that all sketches on a given day should have the same number of values, but this number
 * may increase day after day.
 *
 * @param name            Sketch unique name.
 * @param createTime      Time at which the sketch was created. This is not needed nor actually
 *                        used, but was added to ease debugging.
 * @param clientName      The name of the client this sketch is about.
 * @param campaignName    The name of the campaign this sketch is about.
 * @param group           The identifier of the group the client belongs to for that particular day.
 *                        Groups identifiers can be recycled over different days;
 * @param day             Day this sketch is about (0 = campaign start date).
 * @param publicKey       Public key of the client. This denormalised here in order to avoid an
 *                        additional query when the client pings the server.
 * @param submitted       Whether this sketch has been submitted.
 * @param queriesCount    Size of the campaign vocabulary at the time the sketch was generated.
 *                        Because the vocabulary may evolve at any time, we have to keep track of
 *                        this in order to only ask the clients for the relevant keywords at the
 *                        time the sketch was generated.
 * @param encryptedValues The list of encrypted query counts submitted by the client. This may only
 *                        be filled if `submitted` = true, and will only be filled if the campaign
 *                        is collecting encrypted data. Because values are actually [[BigInt]], they
 *                        are stored as strings.
 * @param rawValues       The list of raw query counts submitted by the client.  This may only be
 *                        filled if `submitted` = true, and will only be filled if the campaign is
 *                        collecting encrypted data.
 */
case class Sketch(
  name: String,
  createTime: Instant,
  clientName: String,
  campaignName: String,
  group: Int,
  day: Int,
  publicKey: String,
  submitted: Boolean,
  queriesCount: Int,
  encryptedValues: Option[Seq[String]] = None,
  rawValues: Option[Seq[Long]] = None)
