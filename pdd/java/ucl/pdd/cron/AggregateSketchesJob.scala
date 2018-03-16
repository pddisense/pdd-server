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

import java.util.UUID

import com.github.nscala_time.time.Imports._
import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import org.joda.time.Instant
import ucl.pdd.api.{Aggregation, AggregationStats, Campaign, Sketch}
import ucl.pdd.config.Timezone
import ucl.pdd.storage.{CampaignStore, SketchStore, Storage}

final class AggregateSketchesJob @Inject()(storage: Storage, @Timezone timezone: DateTimeZone)
  extends Logging {

  def execute(at: Instant): Unit = {
    logger.info(s"Starting ${getClass.getSimpleName}")

    val now = at.toDateTime(timezone)
    val f = storage.campaigns
      .list(CampaignStore.Query(isActive = Some(true)))
      .flatMap(results => Future.join(results.map(handleCampaign(now, _))))
    Await.result(f)

    logger.info(s"Completed ${getClass.getSimpleName}")
  }

  private def handleCampaign(now: DateTime, campaign: Campaign): Future[Unit] = {
    // On a given day `d`, we aggregate the sketches for day `d - 1`.
    // Note: If a campaign is active, its `startTime` is defined.
    val actualDay = (campaign.startTime.get.toDateTime(timezone).withTimeAtStartOfDay to now).duration.days.toInt
    if (actualDay <= 0) {
      Future.Done
    } else {
      handleCampaign(actualDay - 1, campaign)
    }
  }

  private def handleCampaign(day: Int, campaign: Campaign): Future[Unit] = {
    storage
      .sketches
      .list(SketchStore.Query(campaignName = Some(campaign.name)))
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
