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

package ucl.pdd.service

import org.joda.time.{Instant, LocalDate}
import ucl.pdd.domain.{Aggregation, Campaign}

/**
 * Service helping to export aggregations into more human- or machine-readable formats.
 */
final class Exporter {

  import Exporter._

  def collect(campaign: Campaign, results: Seq[Aggregation]): Seq[Row] = {
    val startTime = campaign.startTime.getOrElse(Instant.now())
    results.flatMap { result =>
      if (result.decryptedValues.isEmpty) {
        // If we do not have data for that day, still return the total count.
        Seq(Row(Campaign.absoluteDate(startTime, result.day), "_total", 0))
      } else {
        val total = Row(Campaign.absoluteDate(startTime, result.day), "_total", result.decryptedValues.head)
        val rows = result.decryptedValues
          .tail
          .zipWithIndex
          .filter { case (v, _) => v > 0 }
          .map { case (v, idx) =>
            Row(Campaign.absoluteDate(startTime, result.day), campaign.vocabulary.queries(idx).toString, v)
          }
        total +: rows
      }
    }
  }

  def collectAsCsv(campaign: Campaign, results: Seq[Aggregation]): String = {
    val header = "day,query,count"
    val rows = collect(campaign, results).map { row =>
      s"${row.date},${row.query},${row.count}"
    }
    (header +: rows).mkString("\n")
  }
}

object Exporter {

  case class Row(date: LocalDate, query: String, count: Long)

}
