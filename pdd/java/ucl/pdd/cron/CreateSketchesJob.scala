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
import ucl.pdd.api._
import ucl.pdd.config.{TestingMode, Timezone}
import ucl.pdd.storage.{CampaignStore, ClientStore, Storage}
import ucl.pdd.strategy.{Strategy, StrategyAttrs}

import scala.util.Random

/**
 * Cron job creating each day the relevant sketches for the active campaigns.
 *
 * @param storage     Storage.
 * @param strategy    Groups strategy.
 * @param timezone    Timezone.
 * @param testingMode Is the testing mode enabled?
 */
final class CreateSketchesJob @Inject()(
  storage: Storage,
  strategy: Strategy,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean)
  extends Logging {

  def execute(fireTime: Instant): Unit = {
    logger.info(s"Starting ${getClass.getSimpleName}")

    val now = fireTime.toDateTime(timezone)
    val f = storage.campaigns
      .list(CampaignStore.Query(isActive = Some(true)))
      .flatMap(results => Future.join(results.map(handleCampaign(now, _))))
    Await.result(f)

    logger.info(s"Completed ${getClass.getSimpleName}")
  }

  private def handleCampaign(now: DateTime, campaign: Campaign): Future[Unit] = {
    // On a given day `d`, we create the sketches for day `d - 1`.
    // Note: If a campaign is active, its `startTime` is defined.
    val currentDay = if (testingMode) {
      (campaign.startTime.get.toDateTime(timezone) to now).duration.minutes.toInt / 5
    } else {
      (campaign.startTime.get.toDateTime(timezone).withTimeAtStartOfDay to now).duration.days.toInt
    }
    if (currentDay <= 0) {
      logger.info(s"Nothing to do on day $currentDay")
      Future.Done
    } else {
      handleCampaign(currentDay - 1, campaign)
    }
  }

  private def handleCampaign(day: Int, campaign: Campaign): Future[Unit] = {
    storage
      .clients
      .list(ClientStore.Query(hasLeft = Some(false)))
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
              day = day,
              publicKey = client.publicKey)
            storage.sketches.create(sketch).unit
          }
        }
        Future.collect(fs).unit
      }
  }
}
