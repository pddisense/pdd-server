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

/**
 * An aggregation is a request to collect searches during a given time period as part of a
 * campaign. Each campaign is divided in several aggregations, usually one per day. Throughout the
 * codebase and the documentation, aggregations may also be called "results".
 *
 * @param name            Aggregation unique name.
 * @param campaignName    The name of the campaign this sketch is about.
 * @param day             The day this aggregation is about. It is relative to the associated
 *                        campaign's start time.
 * @param stats           Statistics.
 * @param decryptedValues The list of query counts decrypted from the encrypted counts. The first
 *                        count represents the total number of searches over the associated day
 *                        (all of them, not only the ones actively monitored), and then there is
 *                        one count per monitored query on the associated day.
 * @param rawValues       The list of query counts from the raw counts. The first count represents
 *                        the total number of searches over the associated day (all of them, not
 *                        only the ones actively monitored), and then there is one count per
 *                        monitored query on the associated day.
 */
case class Aggregation(
  name: String,
  campaignName: String,
  day: Int,
  stats: Aggregation.Stats,
  decryptedValues: Seq[Long],
  rawValues: Seq[Long] = Seq.empty) {

  def withoutValues: Aggregation = copy(decryptedValues = Seq.empty, rawValues = Seq.empty)
}

object Aggregation {

  case class Stats(activeCount: Long, submittedCount: Long, decryptedCount: Long)

}
