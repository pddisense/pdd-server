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
  def csv(campaign: Campaign, results: Seq[Aggregation]): String = {
    val lines = if (campaign.collectEncrypted && campaign.collectRaw) {
      val header = "day,query,raw count,decrypted count"
      val content = results.flatMap { result =>
        result.rawValues
          .zip(result.decryptedValues)
          .zipWithIndex
          .map { case ((rawCount, decryptedCount), idx) =>
            s"${result.day},$idx,$rawCount,$decryptedCount"
          }
      }
      header +: content
    } else if (campaign.collectEncrypted) {
      val header = "day,query,decrypted count"
      val content = results.map { result =>
        result.decryptedValues.zipWithIndex.map { case (v, idx) => s"${result.day},$idx,$v" }
      }
      header +: content
    } else if (campaign.collectRaw) {
      val header = "day,query,raw count"
      val content = results.map { result =>
        result.rawValues.zipWithIndex.map { case (v, idx) => s"${result.day},$idx,$v" }
      }
      header +: content
    } else {
      Seq.empty
    }
    lines.mkString("\n")
  }
}
