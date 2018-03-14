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

/**
 * Record the activity of a client. It is later used to create "optimal" groups when dealing with
 * encrypted sketches collection. An activity is recorded when a client sends a ping request
 * to the server, and at most once per day.
 *
 * @param clientName  Client unique identifier.
 * @param countryCode Code of the country the client is currently located in.
 * @param day         Day of the interaction.
 * @param timezone    Timezone the client is currently located in.
 */
case class Activity(clientName: String, countryCode: String, day: Int, timezone: String)
