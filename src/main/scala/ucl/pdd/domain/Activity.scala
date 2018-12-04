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
 * Record the activity of a client. It is later used to create "optimal" groups when dealing with
 * encrypted sketches collection. An activity is recorded when a client sends a ping request
 * to the server. There might be some rate-limiting built on top of it.
 *
 * @param clientName       Client unique identifier.
 * @param time             Time of the interaction.
 * @param countryCode      Code of the country the client is currently located in (ISO 3166-1 alpha-2).
 * @param extensionVersion Version of the Chrome extension the client is actually using.
 * @param timezone         Name of the timezone the client is currently located in.
 */
case class Activity(
  clientName: String,
  time: Instant,
  countryCode: Option[String] = None,
  extensionVersion: Option[String] = None,
  timezone: Option[String] = None)
