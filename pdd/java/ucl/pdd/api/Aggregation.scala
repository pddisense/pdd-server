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
 * An aggregation is a request to collect searches during a given time period as part of a
 * campaign. Each campaign is divided in several aggregations, usually one per day.
 */
case class Aggregation(
  name: String,
  campaignName: String,
  day: Int,
  decryptedValues: Seq[Long],
  rawValues: Seq[Long],
  stats: AggregationStats) {

  def withoutValues: Aggregation = copy(decryptedValues = Seq.empty, rawValues = Seq.empty)
}

case class AggregationStats(
  activeCount: Long,
  submittedCount: Long,
  decryptedCount: Long)
