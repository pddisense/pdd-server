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

import scala.collection.mutable

object CampaignValidator {
  def validate(obj: Campaign): ValidationResult = {
    val errors = mutable.ListBuffer.empty[ErrorCause]
    if (obj.displayName.isEmpty) {
      errors += ErrorCause("Should not be empty", "displayName")
    }
    if (obj.delay < 0) {
      errors += ErrorCause("Should not be negative", "delay")
    }
    if (obj.graceDelay < 0) {
      errors += ErrorCause("Should not be negative", "graceDelay")
    }
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
