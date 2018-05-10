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

package ucl.pdd.server

import ucl.pdd.api.{Aggregation, Campaign}

object Exporter {

  case class Count(
    day: Int,
    query: Int,
    rawCount: Option[Long] = None,
    decryptedCount: Option[Long] = None)

  def json(campaign: Campaign, results: Seq[Aggregation]): Seq[Count] = {
    if (campaign.collectEncrypted && campaign.collectRaw) {
      results.flatMap { result =>
        result.rawValues
          .zip(result.decryptedValues)
          .zipWithIndex
          .filter { case ((rawCount, decryptedCount), _) => rawCount > 0 || decryptedCount > 0 }
          .map { case ((rawCount, decryptedCount), idx) =>
            Count(result.day, idx, Some(rawCount), Some(decryptedCount))
          }
      }
    } else if (campaign.collectEncrypted) {
      results.flatMap { result =>
        result.decryptedValues
          .zipWithIndex
          .filter { case (v, _) => v > 0 }
          .map { case (v, idx) => Count(result.day, idx, decryptedCount = Some(v)) }
      }
    } else if (campaign.collectRaw) {
      results.flatMap { result =>
        result.rawValues
          .zipWithIndex
          .filter { case (v, _) => v > 0 }
          .map { case (v, idx) => Count(result.day, idx, rawCount = Some(v)) }
      }
    } else {
      Seq.empty
    }
  }

  def csv(campaign: Campaign, results: Seq[Aggregation]): String = {
    val header = "day,query,raw count,decrypted count"
    val lines = json(campaign, results).map { count =>
      s"${count.day},${count.query},${count.rawCount.getOrElse("")},${count.decryptedCount.getOrElse("")}"
    }
    (header +: lines).mkString("\n")
  }
}
