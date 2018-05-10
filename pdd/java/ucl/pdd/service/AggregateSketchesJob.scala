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
import ucl.pdd.api.{Aggregation, AggregationStats, Campaign, Sketch}
import ucl.pdd.storage.{SketchStore, Storage}

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
      .flatMap(results => Future.join(results.map(handleCampaign(now, _))))
    Await.result(f)

    logger.info(s"$prefix Completed job")
  }

  private def handleCampaign(now: DateTime, campaign: Campaign): Future[Unit] = {
    // On a given day `d`, we aggregate the sketches from day `d - 2 - campaign.delay - campaign.graceDelay`
    // to `d - 2 - campaign.delay`.
    campaign.startTime match {
      case None =>
        // This campaign was never started, nothing to do.
        Future.Done
      case Some(startTime) =>
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
          Future.join(days.map(aggregate(_, campaign)))
        }
      //TODO: backfill.
      //TODO: clean old sketches.
    }
  }

  private def aggregate(day: Int, campaign: Campaign): Future[Unit] = {
    storage
      .sketches
      .list(SketchStore.Query(campaignName = Some(campaign.name), day = Some(day)))
      .flatMap(sketches => aggregate(day, campaign, sketches))
  }

  private def aggregate(day: Int, campaign: Campaign, sketches: Seq[Sketch]): Future[Unit] = {
    val rawValues = if (campaign.collectRaw) {
      foldRaw(sketches.map(_.rawValues.toSeq.flatten))
    } else {
      Seq.empty
    }
    val (decryptedValues, decryptedCount) = if (campaign.collectEncrypted) {
      // TODO: raise a warning if submitted but no encrypted values?
      val decryptedByGroup = sketches
        .groupBy(_.group)
        .values
        .filter(_.forall(s => s.submitted && s.encryptedValues.isDefined))
      val valuesByGroup = decryptedByGroup.map { groupSketches =>
        foldEncrypted(groupSketches.map(_.encryptedValues.toSeq.flatten))
      }
      (foldRaw(valuesByGroup), decryptedByGroup.map(_.size).sum.toLong)
    } else {
      (Seq.empty, 0L)
    }

    val aggregation = Aggregation(
      name = s"${campaign.name}-$day",
      campaignName = campaign.name,
      day = day,
      decryptedValues = decryptedValues,
      rawValues = rawValues,
      stats = AggregationStats(
        activeCount = sketches.size,
        submittedCount = sketches.count(_.submitted),
        decryptedCount = decryptedCount))

    storage.aggregations
      .get(s"${campaign.name}-$day")
      .map {
        case Some(_) => storage.aggregations.replace(aggregation)
        case None => storage.aggregations.create(aggregation)
      }
      .onSuccess { _ =>
        logger.info(s"$prefix Aggregated ${sketches.size} sketches for campaign ${campaign.name} (day: $day)")
      }
      .unit
  }

  private def foldRaw(values: Iterable[Seq[Long]]): Seq[Long] = {
    val nonEmptyValues = values.filter(_.nonEmpty)
    if (nonEmptyValues.isEmpty) {
      Seq.empty
    } else {
      nonEmptyValues.reduce[Seq[Long]] { case (a, b) =>
        a.zip(b).map { case (n1, n2) => n1 + n2 }
      }
    }
  }

  private def foldEncrypted(values: Iterable[Seq[String]]): Seq[Long] = {
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
