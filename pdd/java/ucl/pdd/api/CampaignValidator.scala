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

package ucl.pdd.api

import org.joda.time.Instant

import scala.collection.mutable

object CampaignValidator {
  def validate(obj: Campaign): ValidationResult = {
    val errors = mutable.ListBuffer.empty[ErrorCause]
    if (obj.startTime.zip(obj.endTime).exists { case (a, b) => b.isBefore(a) }) {
      errors += ErrorCause("Should be greater than or equal to `startTime`", "endTime")
    }
    if (obj.samplingRate.exists(samplingRate => samplingRate < 0 || samplingRate > 1)) {
      errors += ErrorCause("Should be between 0 and 1", "samplingRate")
    }
    if (errors.isEmpty) ValidationResult.Valid else ValidationResult.Invalid(errors.toList)
  }

  def validateUpdate(obj: Campaign, previous: Campaign): ValidationResult = {
    val errors = mutable.ListBuffer.empty[ErrorCause]

    // Most fields become read-only once a campaign is started.
    if (previous.isStarted && obj.startTime != previous.startTime) {
      errors += ErrorCause("Cannot be changed once a campaign started", "startTime")
    }
    if (previous.isStarted && obj.endTime.exists(_.isBefore(Instant.now))) {
      errors += ErrorCause("Cannot be set in the past once a campaign started", "endTime")
    }
    if (previous.isStarted && obj.delay != previous.delay) {
      errors += ErrorCause("Cannot be changed once a campaign started", "delay")
    }
    if (previous.isStarted && obj.graceDelay != previous.graceDelay) {
      errors += ErrorCause("Cannot be changed once a campaign started", "graceDelay")
    }
    if (previous.isStarted && obj.collectRaw != previous.collectRaw) {
      errors += ErrorCause("Cannot be changed once a campaign started", "collectRaw")
    }
    if (previous.isStarted && obj.collectEncrypted != previous.collectEncrypted) {
      errors += ErrorCause("Cannot be changed once a campaign started", "collectEncrypted")
    }
    if (previous.isStarted && obj.groupSize != previous.groupSize) {
      errors += ErrorCause("Cannot be changed once a campaign started", "groupSize")
    }

    // Enforce the vocabulary is append-only.
    if (obj.vocabulary.queries.size < previous.vocabulary.queries.size) {
      errors += ErrorCause("Vocabulary is append-only, queries cannot be removed", "vocabulary.queries")
    } else if (obj.vocabulary.queries.take(previous.vocabulary.queries.size) != previous.vocabulary.queries) {
      errors += ErrorCause("Vocabulary is append-only, previous queries cannot be changed", "vocabulary.queries")
    }

    val result = if (errors.isEmpty) ValidationResult.Valid else ValidationResult.Invalid(errors.toList)
    result ++ validate(obj)
  }
}
