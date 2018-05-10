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

import java.util.UUID

import com.github.nscala_time.time.Imports._
import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.storage.{CampaignStore, Storage}
import ucl.pdd.strategy.{Strategy, StrategyAttrs}

import scala.util.Random

/**
 * Cron job creating each day the relevant sketches for the active campaigns.
 *
 * @param storage     Storage.
 * @param strategy    Groups strategy.
 * @param timezone    Current timezone.
 * @param testingMode Is the testing mode enabled?
 */
final class CreateSketchesJob @Inject()(
  storage: Storage,
  strategy: Strategy,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean)
  extends Logging {

  private[this] val prefix = s"[${getClass.getSimpleName}]"

  def execute(fireTime: Instant): Unit = {
    logger.info(s"$prefix Starting job")

    val now = fireTime.toDateTime(timezone)
    val f = storage.campaigns
      .list(CampaignStore.Query(isActive = Some(true)))
      .flatMap(results => Future.join(results.map(handleCampaign(now, _))))
    Await.result(f)

    logger.info(s"$prefix Completed job")
  }

  private def handleCampaign(now: DateTime, campaign: Campaign): Future[Unit] = {
    // On a given day `d`, we create the sketches for day `d - 1`.
    // Note: If a campaign is active, its `startTime` is defined.
    val currentDay = if (testingMode) {
      (campaign.startTime.get.toDateTime(timezone) to now).duration.minutes.toInt / 5
    } else {
      (campaign.startTime.get.toDateTime(timezone).withTimeAtStartOfDay to now).duration.days.toInt
    }
    val day = currentDay - 1
    if (day < 0) {
      logger.info(s"$prefix Nothing to do for campaign ${campaign.name} (just started)")
      Future.Done
    } else {
      handleCampaign(day, campaign)
    }
  }

  private def handleCampaign(day: Int, campaign: Campaign): Future[Unit] = {
    storage
      .clients
      .list()
      .flatMap { clients =>
        val sampledClients = campaign.samplingRate match {
          case None | Some(1d) => clients
          case Some(samplingRate) => clients.filter(_ => Random.nextDouble() < samplingRate)
        }
        val groups = strategy.apply(sampledClients, StrategyAttrs(campaign.groupSize))
        val fs = groups.zipWithIndex.flatMap { case (groupClients, groupIdx) =>
          groupClients.map { client =>
            val sketch = Sketch(
              name = UUID.randomUUID().toString,
              clientName = client.name,
              campaignName = campaign.name,
              group = groupIdx,
              submitted = false,
              day = day,
              publicKey = client.publicKey)
            storage.sketches.create(sketch).unit
          }
        }
        Future
          .collect(fs)
          .onSuccess { res =>
            logger.info(s"$prefix Created ${res.size} sketches for campaign ${campaign.name} (day $day)")
          }
          .unit
      }
  }
}
