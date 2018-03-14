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
 * A client corresponds to a registered browser extension. We do not track users across multiple
 * browsers, i.e, they would each behave as a different client.
 *
 * @param name         Client name, unique among all clients.
 * @param createTime   Time at which the client was created.
 * @param publicKey    Public key.
 * @param browser      Identifier of the browser used by this client.
 * @param externalName A name voluntarily filled by the user allowing to identify him.
 * @param leaveTime    Time at which the client left the campaign. Shall he choose to come back, he
 *                     will be considered as a new client.
 */
case class Client(
  name: String,
  createTime: Instant,
  publicKey: String,
  browser: String,
  externalName: Option[String] = None,
  leaveTime: Option[Instant] = None) {

  def hasLeft: Boolean = leaveTime.nonEmpty
}
