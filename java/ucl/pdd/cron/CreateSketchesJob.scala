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
import ucl.pdd.api._
import ucl.pdd.config.DayDuration
import ucl.pdd.storage.{CampaignQuery, ClientQuery, Storage}
import ucl.pdd.strategy.{Strategy, StrategyAttrs}

import scala.util.Random

/**
 * This cron job create each day the relevant sketches for the active campaigns.
 *
 * @param storage
 * @param strategy
 * @param dayDuration
 */
final class CreateSketchesJob @Inject()(
  storage: Storage,
  strategy: Strategy,
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
    // On a given day `d`, we create the sketches for day `d - 1`.
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
      .clients
      .list(ClientQuery(hasLeft = Some(false)))
      .flatMap { clients =>
        val sampledClients = campaign.samplingRate match {
          case None => clients
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
            storage.sketches.save(sketch).unit
          }
        }
        Future.collect(fs).unit
      }
  }
}
