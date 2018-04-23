/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
