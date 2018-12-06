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

import com.google.inject.{Inject, Singleton}
import com.twitter.inject.Logging
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.domain.{Aggregation, Campaign, Sketch}
import ucl.pdd.storage.{SketchStore, Storage}

/**
 * The role of this job is to periodically aggregate the pending sketches. While a [[Sketch]]
 * represent data sent by the clients, an [[Aggregation]] is what is ultimately presented to the
 * analysts. Sketches are expected to be garbage-collected once the relevant aggregations have been
 * formed and the campaign's grace delay is expired.
 *
 * @param storage Storage.
 */
@Singleton
final class AggregateSketchesJob @Inject()(storage: Storage)
  extends Job with Logging {

  override def execute(fireTime: Instant): Future[Unit] = {
    storage.campaigns.list().flatMap { results =>
      Future.join(results.map(aggregate(_, fireTime)))
    }
  }

  private def aggregate(campaign: Campaign, now: Instant): Future[Unit] = {
    campaign.startTime match {
      case None =>
        // This campaign is not started, nothing to do.
        Future.Done
      case Some(startTime) =>
        // On a given day `d`, we aggregate the sketches from day `d - 2 - campaign.delay -
        // campaign.graceDelay` to `d - 2 - campaign.delay`.
        //
        // Indeed, it takes at least one full day to collect the sketches of the previous day
        // (given there is no delay). Therefore, on a given day `d`, we are collecting the sketches
        // for day `d - 1`, which means we can expect them to be available on `d + 1`.
        //
        // If we have further delay, we do not compute the sketches before that delay is elapsed
        // (because the aggregations won't be available anyway). The grace delay means that the
        // aggregations are made available but might still evolve. We hence need to recompute those
        // every day during during the grace delay window.
        val actualDay = Campaign.relativeDay(startTime, now)
        val endDay = actualDay - 2 - campaign.delay
        if (endDay < 0) {
          info(s"Nothing to do for campaign ${campaign.name} (just started)")
          Future.Done
        } else {
          val startDay = math.max(0, endDay - campaign.graceDelay)
          val days = startDay to endDay
          Future.join(days.map(aggregate(campaign, _)))
        }
      //TODO: don't do it for stopped campaigns, once graceDelay is past.
      //TODO: clean old sketches.
    }
  }

  private def aggregate(campaign: Campaign, day: Int): Future[Unit] = {
    storage.sketches
      .list(SketchStore.Query(campaignName = Some(campaign.name), day = Some(day)))
      .flatMap(sketches => aggregate(campaign, day, sketches))
  }

  private def aggregate(campaign: Campaign, day: Int, sketches: Seq[Sketch]): Future[Unit] = {
    val rawValues = if (ServiceModule.FlagCollectRaw) {
      // Raw values are by default not collected, for privacy reasons.
      sumRawValues(sketches.map(_.rawValues.toSeq.flatten))
    } else {
      Seq.empty
    }
    // TODO: raise a warning if submitted but no encrypted values?
    // Encrypted values of a group can only be decrypted if in possession of all values of this
    // group. First step is then to filter the groups for which we have all submissions.
    val decryptedByGroup = sketches
      .groupBy(_.group)
      .values
      .filter(_.forall(s => s.submitted && s.encryptedValues.isDefined))
    val valuesByGroup = decryptedByGroup.map { groupSketches =>
      sumEncryptedValues(groupSketches.map(_.encryptedValues.toSeq.flatten))
    }
    val decryptedValues = sumRawValues(valuesByGroup)
    val decryptedCount = decryptedByGroup.map(_.size).sum.toLong

    val aggregation = Aggregation(
      name = s"${campaign.name}-$day",
      campaignName = campaign.name,
      day = day,
      decryptedValues = decryptedValues,
      rawValues = rawValues,
      stats = Aggregation.Stats(
        activeCount = sketches.size,
        submittedCount = sketches.count(_.submitted),
        decryptedCount = decryptedCount))

    storage.aggregations
      .get(aggregation.name)
      .map {
        case Some(_) => storage.aggregations.replace(aggregation)
        case None => storage.aggregations.create(aggregation)
      }
      .onSuccess { _ =>
        info(s"Aggregated ${sketches.size} sketches for campaign ${campaign.name} (day: $day)")
      }
      .unit
  }

  private def sumRawValues(values: Iterable[Seq[Long]]): Seq[Long] = {
    val nonEmptyValues = values.filter(_.nonEmpty)
    if (nonEmptyValues.isEmpty) {
      Seq.empty
    } else {
      nonEmptyValues.reduce[Seq[Long]] { case (a, b) => a.zip(b).map { case (n1, n2) => n1 + n2 } }
    }
  }

  private def sumEncryptedValues(values: Iterable[Seq[String]]): Seq[Long] = {
    val nonEmptyValues = values.filter(_.nonEmpty)
    if (nonEmptyValues.isEmpty) {
      Seq.empty
    } else {
      nonEmptyValues
        .map(_.map(BigInt.apply))
        .reduce[Seq[BigInt]] { case (a, b) => a.zip(b).map { case (n1, n2) => n1 + n2 } }
        .map(_.toLong)
    }
  }
}
