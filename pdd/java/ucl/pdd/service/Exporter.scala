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

import ucl.pdd.domain.{Aggregation, Campaign}

object Exporter {

  case class Count(
    day: Int,
    query: String,
    rawCount: Option[Long] = None,
    decryptedCount: Option[Long] = None)

  def collect(campaign: Campaign, results: Seq[Aggregation]): Seq[Count] = {
    if (ServiceModule.FlagCollectRaw) {
      collectBoth(results)
    } else  {
      collectEncrypted(results)
    }
  }

  def csv(campaign: Campaign, results: Seq[Aggregation]): String = {
    val header = "day,query,raw count,decrypted count"
    val lines = collect(campaign, results).map { count =>
      s"${count.day},${count.query},${count.rawCount.getOrElse("")},${count.decryptedCount.getOrElse("")}"
    }
    (header +: lines).mkString("\n")
  }

  private def collectBoth(results: Seq[Aggregation]): Seq[Count] = {
    results.flatMap { result =>
      if (result.decryptedValues.isEmpty && result.rawValues.isEmpty) {
        // If for any reason we do not have data for that day, still return the total count.
        Seq(Count(result.day, "total", rawCount = Some(0), decryptedCount = Some(0)))
      } else if (result.decryptedValues.isEmpty) {
        // There is an edge case, where the raw values are filled and the decrypted values are
        // not filled. This happens when no value could be decrypted, the list might end up empty
        // (instead of full of zeros).
        val total = Count(result.day, "total", rawCount = Some(result.rawValues.head), decryptedCount = Some(0))
        val rows = result.rawValues.tail
          .zipWithIndex
          .filter { case (v, _) => v > 0 }
          .map { case (v, idx) =>
            Count(result.day, idx.toString, rawCount = Some(v), decryptedCount = Some(0))
          }
        total +: rows
      } else {
        val counts = result.rawValues.zip(result.decryptedValues)
        val total = Count(result.day, "total", rawCount = Some(counts.head._1), decryptedCount = Some(counts.head._2))
        val rows = counts.tail
          .zipWithIndex
          .filter { case ((rawCount, decryptedCount), _) => rawCount > 0 || decryptedCount > 0 }
          .map { case ((rawCount, decryptedCount), idx) =>
            Count(result.day, idx.toString, rawCount = Some(rawCount), decryptedCount = Some(decryptedCount))
          }
        total +: rows
      }
    }
  }

  private def collectEncrypted(results: Seq[Aggregation]): Seq[Count] = {
    results.flatMap { result =>
      if (result.decryptedValues.isEmpty) {
        // If for any reason we do not have data for that day, still return the total count.
        Seq(Count(result.day, "total", decryptedCount = Some(0)))
      } else {
        val total = Count(result.day, "total", decryptedCount = Some(result.decryptedValues.head))
        val rows = result.decryptedValues.tail
          .zipWithIndex
          .filter { case (v, _) => v > 0 }
          .map { case (v, idx) => Count(result.day, idx.toString, decryptedCount = Some(v)) }
        total +: rows
      }
    }
  }
}
