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

package ucl.pdd.cron

import java.util.UUID

import com.github.nscala_time.time.Imports._
import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import org.joda.time.Instant
import org.quartz.{Job, JobExecutionContext}
import ucl.pdd.api.{Aggregation, AggregationStats, Campaign, Sketch}
import ucl.pdd.config.DayDuration
import ucl.pdd.storage.{CampaignQuery, SketchQuery, Storage}

final class AggregateSketchesJob @Inject()(
  storage: Storage,
  @DayDuration dayDuration: Duration)
  extends Job with Logging {

  override def execute(jobExecutionContext: JobExecutionContext): Unit = {
    logger.info(s"Starting ${getClass.getSimpleName}")

    val now = new Instant(jobExecutionContext.getFireTime.getTime)
    val f = storage.campaigns
      .list(CampaignQuery(isActive = Some(true)))
      .flatMap(results => Future.join(results.map(handleCampaign(now, _))))
    Await.result(f)

    logger.info(s"Completed ${getClass.getSimpleName}")
  }

  private def handleCampaign(now: Instant, campaign: Campaign): Future[Unit] = {
    // On a given day `d`, we aggregate the sketches for day `d - 1`.
    // Note: If a campaign is active, its `startTime` is defined.
    val actualDay = ((now to campaign.startTime.get).millis / dayDuration.millis).toInt
    if (actualDay <= 0) {
      Future.Done
    } else {
      handleCampaign(actualDay - 1, campaign)
    }
  }

  private def handleCampaign(day: Int, campaign: Campaign): Future[Unit] = {
    storage
      .sketches
      .list(SketchQuery(campaignName = Some(campaign.name)))
      .flatMap { sketches =>
        val f = createAggregation(sketches, day, campaign)
        f.flatMap { _ =>
          val fs = sketches.map(sketch => storage.sketches.delete(sketch.name))
          Future.join(fs)
        }
      }
  }

  private def createAggregation(sketches: Seq[Sketch], day: Int, campaign: Campaign): Future[Unit] = {
    val rawValues = if (campaign.collectRaw) {
      foldRaw(sketches.map(_.rawValues.toSeq.flatten))
    } else {
      Seq.empty
    }
    val (decryptedValues, decryptedCount) = if (campaign.collectEncrypted) {
      val groupValues = sketches.groupBy(_.group).map { case (_, groupSketches) =>
        if (groupSketches.forall(_.isSubmitted)) {
          foldEncrypted(groupSketches.map(_.encryptedValues.toSeq.flatten))
        } else {
          Seq.empty
        }
      }
      (foldRaw(groupValues), groupValues.map(_.size).sum.toLong)
    } else {
      (Seq.empty, 0L)
    }
    val stats = AggregationStats(
      activeCount = sketches.size,
      submittedCount = sketches.count(_.isSubmitted),
      decryptedCount = decryptedCount)
    val aggregation = Aggregation(
      name = UUID.randomUUID().toString,
      campaignName = campaign.name,
      day = day,
      decryptedValues = decryptedValues,
      rawValues = rawValues,
      stats = stats)
    storage.aggregations.create(aggregation).unit
  }

  private def foldRaw(values: Iterable[Seq[Long]]): Seq[Long] = {
    if (values.isEmpty) {
      Seq.empty
    } else {
      values.reduce[Seq[Long]] { case (a, b) =>
        a.zip(b).map { case (n1, n2) => n1 + n2 }
      }
    }
  }

  private def foldEncrypted(values: Iterable[Seq[String]]): Seq[Long] = {
    if (values.isEmpty) {
      Seq.empty
    } else {
      values
        .map(_.map(BigInt.apply))
        .reduce[Seq[BigInt]] { case (a, b) => a.zip(b).map { case (n1, n2) => n1 + n2 } }
        .map(_.toLong)
    }
  }
}
