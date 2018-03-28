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

package ucl.pdd.cron

import java.util.concurrent.locks.ReentrantLock

import com.github.nscala_time.time.Imports._
import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import org.joda.time.Instant
import ucl.pdd.api.{Aggregation, AggregationStats, Campaign, Sketch}
import ucl.pdd.config.{TestingMode, Timezone}
import ucl.pdd.storage.{SketchStore, Storage}

final class AggregateSketchesJob @Inject()(
  storage: Storage,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean)
  extends Logging {

  private[this] val lock = new ReentrantLock()

  def execute(fireTime: Instant): Unit = {
    lock.lock()
    try {
      logger.info(s"Starting ${getClass.getSimpleName}")
      val now = fireTime.toDateTime(timezone)
      val f = storage.campaigns
        .list()
        .flatMap(results => Future.join(results.map(handleCampaign(now, _))))
      Await.result(f)
    } finally {
      lock.unlock()
      logger.info(s"Completed ${getClass.getSimpleName}")
    }
  }

  private def handleCampaign(now: DateTime, campaign: Campaign): Future[Unit] = {
    // On a given day `d`, we aggregate the sketches from day `d - 1 - campaign.delay - campaign.graceDelay`
    // to `d - 1 - campaign.delay`.
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
        if (actualDay - campaign.delay <= 0) {
          Future.Done
        } else {
          val startDay = actualDay - 1 - campaign.delay - campaign.graceDelay
          val days = math.max(0, startDay) to (actualDay - 1 - campaign.delay)
          Future.join(days.map(aggregate(_, campaign)))
        }
        //TODO: backfill.
        //TODO: clean old sketches.
    }
  }

  private def aggregate(day: Int, campaign: Campaign): Future[Unit] = {
    storage.sketches
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
      val decryptedByGroup = sketches.groupBy(_.group).values.filter(_.forall(s => s.isSubmitted && s.encryptedValues.isDefined))
      val groupValues = decryptedByGroup.map { groupSketches =>
        foldEncrypted(groupSketches.map(_.encryptedValues.toSeq.flatten))
      }
      (foldRaw(groupValues), groupValues.map(_.size).sum.toLong)
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
        submittedCount = sketches.count(_.isSubmitted),
        decryptedCount = decryptedCount))

    storage.aggregations
      .get(s"${campaign.name}-$day")
      .map {
        case Some(_) => storage.aggregations.replace(aggregation)
        case None => storage.aggregations.create(aggregation)
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
