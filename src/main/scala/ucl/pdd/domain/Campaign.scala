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

import com.github.nscala_time.time.Imports._
import org.joda.time.Instant

/**
 * A campaign corresponds to a vocabulary tracked during a given period of time.
 *
 * @param name             Campaign unique name.
 * @param createTime       Time at which the campaign was created.
 * @param displayName      Human-readable name of this campaign.
 * @param email            E-mail address associated with this campaign.
 * @param notes            Notes describing the purpose of this campaign.
 * @param vocabulary       Last version of the vocabulary tracked by this campaign. We do not keep
 *                         a history of the vocabularies here, this is implicitly tracked by the
 *                         various aggregations created as part of this campaign.
 * @param startTime        Time at which this campaign starts.
 * @param endTime          Time at which this campaign completes. After this time, no new
 *                         aggregations will be generated for this campaign, but currently active
 *                         aggregations will remain active for at most `delay` + `graceDelay`. It
 *                         can be left empty for an open-ended campaign.
 * @param delay            Delay after which the aggregation is made available.
 * @param graceDelay       Additional delay during which the sketches are still accepted, thus
 *                         allowing the aggregation to be refined. This delay starts after the
 *                         initial `delay` is expired.
 * @param groupSize        Expected group size. It is a hint for the group forming strategy as what the
 *                         size of a group should be. In practice, it is an upper bound.
 * @param samplingRate     A sampling rate to apply when creating aggregations. It means that only
 *                         a subset of clients will be considered and integrated into groups.
 */
case class Campaign(
  name: String,
  createTime: Instant,
  displayName: String,
  email: Option[String] = None,
  notes: Option[String] = None,
  vocabulary: Vocabulary,
  startTime: Option[Instant] = None,
  endTime: Option[Instant] = None,
  delay: Int,
  graceDelay: Int,
  groupSize: Int,
  samplingRate: Option[Double] = None) {

  def withoutVocabulary: Campaign = copy(vocabulary = Vocabulary())

  def isStarted: Boolean = startTime.exists(_ < Instant.now)

  def isCompleted: Boolean = endTime.exists(_ < Instant.now)

  def isActive: Boolean = isStarted && !isCompleted
}

object Campaign {
  def relativeDay(startTime: Instant, now: Instant): Int = {
    (startTime.toDateTime.withTimeAtStartOfDay to now.toDateTime).duration.days.toInt
  }

  def absoluteInstant(startTime: Instant, day: Int): Instant = {
    (startTime.toDateTime.withTimeAtStartOfDay + day.days).toInstant
  }

  def absoluteDate(startTime: Instant, day: Int): LocalDate = {
    (startTime.toDateTime.withTimeAtStartOfDay + day.days).toLocalDate
  }
}
