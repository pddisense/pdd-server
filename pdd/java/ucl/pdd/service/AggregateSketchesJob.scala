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

import com.github.nscala_time.time.Imports._
import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import org.joda.time.Instant
import ucl.pdd.domain.{Aggregation, Campaign, Sketch}
import ucl.pdd.storage.{SketchStore, Storage}

/**
 * The role of this job is to periodically aggregate the pending sketches. While a [[Sketch]]
 * represent data sent by the clients, an [[Aggregation]] is what is ultimately presented to the
 * analysts. Sketches are expected to be garbage-collected once the relevant aggregations have been
 * formed and the campaign's grace delay is expired.
 *
 * @param storage     Persistent storage.
 * @param timezone    Reference timezone.
 * @param testingMode Whether we are in testing mode, where days are shorter.
 */
final class AggregateSketchesJob @Inject()(
  storage: Storage,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean)
  extends Logging {

  private[this] val prefix = s"[${getClass.getSimpleName}]"

  def execute(fireTime: Instant): Unit = synchronized {
    logger.info(s"$prefix Starting job")

    val now = fireTime.toDateTime(timezone)
    val f = storage.campaigns
      .list()
      .flatMap(results => Future.join(results.map(aggregate(_, now))))
    Await.result(f)

    logger.info(s"$prefix Completed job")
  }

  private def aggregate(campaign: Campaign, now: DateTime): Future[Unit] = {
    campaign.startTime match {
      case None =>
        // This campaign was never started, nothing to do.
        Future.Done
      case Some(startTime) =>
        // On a given day `d`, we aggregate the sketches from day `d - 2 - campaign.delay -
        // campaign.graceDelay` to `d - 2 - campaign.delay`.
        //
        // Indeed, it takes at least one full day to collect the sketches of the previous day
        // (given there is no delay). Therefore, on a given day `d`, we are collecting the sketches
        // for day `d - 1`, which means we can expect them to be available on `d + 1` (hence the
        // base `d - 2` in the above formula.
        // If we have further delay, we do not compute the sketches before that delay is elapsed
        // (because the aggregations won't be available anyway). The grace delay means that the
        // aggregations are made available but might still evolve. We hence need to recompute those
        // every during during the grace delay window.
        val actualDay = if (testingMode) {
          (startTime.toDateTime(timezone) to now).duration.minutes.toInt / 5
        } else {
          (startTime.toDateTime(timezone).withTimeAtStartOfDay to now).duration.days.toInt
        }
        val endDay = actualDay - 2 - campaign.delay
        if (endDay < 0) {
          logger.info(s"$prefix Nothing to do for campaign ${campaign.name} (just started)")
          Future.Done
        } else {
          val startDay = math.max(0, actualDay - 2 - campaign.delay - campaign.graceDelay)
          val days = startDay to endDay
          Future.join(days.map(aggregate(campaign, _)))
        }
      //TODO: backfill.
      //TODO: clean old sketches.
    }
  }

  private def aggregate(campaign: Campaign, day: Int): Future[Unit] = {
    storage
      .sketches
      .list(SketchStore.Query(campaignName = Some(campaign.name), day = Some(day)))
      .flatMap(sketches => aggregate(campaign, day, sketches))
  }

  private def aggregate(campaign: Campaign, day: Int, sketches: Seq[Sketch]): Future[Unit] = {
    // We only aggregate raw values if the campaign was expecting so.
    val rawValues = if (campaign.collectRaw) {
      sumRawValues(sketches.map(_.rawValues.toSeq.flatten))
    } else {
      Seq.empty
    }
    // We only aggregate and decrypt encrypted values if the campaign was expecting so.
    val (decryptedValues, decryptedCount) = if (campaign.collectEncrypted) {
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
      (sumRawValues(valuesByGroup), decryptedByGroup.map(_.size).sum.toLong)
    } else {
      (Seq.empty, 0L)
    }

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
        logger.info(s"$prefix Aggregated ${sketches.size} sketches for campaign ${campaign.name} (day: $day)")
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
