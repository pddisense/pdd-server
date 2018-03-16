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
    obj.email.zipWithIndex.foreach { case (email, idx) =>
      validateTerm(s"email.$idx", email, errors)
    }
    obj.vocabulary.queries.zipWithIndex.foreach { case (query, idx) =>
      query match {
        case VocabularyQuery(Some(exact), None) =>
          validateTerm(s"vocabulary.queries.$idx.exact", exact, errors)
        case VocabularyQuery(None, Some(terms)) =>
          terms.zipWithIndex.foreach { case (term, idx2) =>
            validateTerm(s"vocabulary.queries.$idx.terms.$idx2", term, errors)
          }
        case VocabularyQuery(None, None) =>
          errors += ErrorCause("should specify a query", s"vocabulary.queries.$idx")
        case VocabularyQuery(Some(_), Some(_)) =>
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
    if (previous.isStarted && obj.endTime.exists(_.isBefore(Instant.now))) {
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
    if (previous.isStarted && obj.groupSize != previous.groupSize) {
      errors += ErrorCause("cannot be changed once a campaign started", "groupSize")
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
