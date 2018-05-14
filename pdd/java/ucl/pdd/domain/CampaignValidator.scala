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

import org.joda.time.Instant

import scala.collection.mutable

object CampaignValidator {
  def validate(obj: Campaign): ValidationResult = {
    val errors = mutable.ListBuffer.empty[ErrorCause]
    if (obj.displayName.isEmpty) {
      errors += ErrorCause("should not be empty", "displayName")
    }
    if (obj.delay < 0) {
      errors += ErrorCause("should not be negative", "delay")
    }
    if (obj.graceDelay < 0) {
      errors += ErrorCause("should not be negative", "graceDelay")
    }
    if (obj.groupSize < 1) {
      errors += ErrorCause("should be at least 1", "groupSize")
    }
    if (obj.startTime.zip(obj.endTime).exists { case (a, b) => b.isBefore(a) }) {
      errors += ErrorCause("should be greater than or equal to start time", "endTime")
    }
    if (obj.samplingRate.exists(samplingRate => samplingRate < 0 || samplingRate > 1)) {
      errors += ErrorCause("should be between 0 and 1", "samplingRate")
    }
    obj.email.foreach(email => validateTerm("email", email, errors))
    obj.vocabulary.queries.zipWithIndex.foreach { case (query, idx) =>
      query match {
        case Vocabulary.Query(Some(exact), None) =>
          validateTerm(s"vocabulary.queries.$idx.exact", exact, errors)
        case Vocabulary.Query(None, Some(terms)) =>
          terms.zipWithIndex.foreach { case (term, idx2) =>
            validateTerm(s"vocabulary.queries.$idx.terms.$idx2", term, errors)
          }
        case Vocabulary.Query(None, None) =>
          errors += ErrorCause("should specify a query", s"vocabulary.queries.$idx")
        case Vocabulary.Query(Some(_), Some(_)) =>
          errors += ErrorCause("should specify either an exact or terms query, not both", s"vocabulary.queries.$idx")
      }
    }
    if (errors.isEmpty) ValidationResult.Valid else ValidationResult.Invalid(errors.toList)
  }

  def validateUpdate(obj: Campaign, previous: Campaign): ValidationResult = {
    val errors = mutable.ListBuffer.empty[ErrorCause]

    // Most fields become read-only once a campaign is started.
    if (previous.isStarted && obj.startTime != previous.startTime) {
      errors += ErrorCause("cannot be changed once a campaign started", "startTime")
    }
    if (previous.isCompleted && obj.endTime != previous.endTime) {
      errors += ErrorCause("cannot be changed in the past once a campaign completed", "endTime")
    } else if (previous.isStarted && obj.endTime.exists(_.isBefore(Instant.now))) {
      errors += ErrorCause("cannot be set in the past once a campaign started", "endTime")
    }
    if (previous.isStarted && obj.delay != previous.delay) {
      errors += ErrorCause("cannot be changed once a campaign started", "delay")
    }
    if (previous.isStarted && obj.graceDelay != previous.graceDelay) {
      errors += ErrorCause("cannot be changed once a campaign started", "graceDelay")
    }
    if (previous.isStarted && obj.collectRaw != previous.collectRaw) {
      errors += ErrorCause("cannot be changed once a campaign started", "collectRaw")
    }
    if (previous.isStarted && obj.collectEncrypted != previous.collectEncrypted) {
      errors += ErrorCause("cannot be changed once a campaign started", "collectEncrypted")
    }
    if (previous.isStarted && obj.samplingRate != previous.samplingRate) {
      errors += ErrorCause("cannot be changed in the past once a campaign started", "samplingRate")
    }
    if (previous.isStarted && obj.groupSize != previous.groupSize) {
      errors += ErrorCause("cannot be changed in the past once a campaign started", "groupSize")
    }

    // Enforce the vocabulary is append-only.
    if (obj.vocabulary.queries.size < previous.vocabulary.queries.size) {
      errors += ErrorCause("vocabulary is append-only, queries cannot be removed", "vocabulary.queries")
    } else if (obj.vocabulary.queries.take(previous.vocabulary.queries.size) != previous.vocabulary.queries) {
      errors += ErrorCause("vocabulary is append-only, previous queries cannot be changed", "vocabulary.queries")
    }

    val result = if (errors.isEmpty) ValidationResult.Valid else ValidationResult.Invalid(errors.toList)
    result ++ validate(obj)
  }

  private def validateTerm(field: String, term: String, errors: mutable.ListBuffer[ErrorCause]): Unit = {
    if (term.trim.isEmpty) {
      errors += ErrorCause("should not be empty", field)
    }
    // The comma and line break is both forbidden because they may be used to differentiate between
    // elements of the same list (outside of a JSON context).
    if (term.contains(',')) {
      errors += ErrorCause("cannot contain a comma (reserved character)", field)
    }
    if (term.contains('\n')) {
      errors += ErrorCause("cannot contain a line break (reserved character)", field)
    }
  }
}
