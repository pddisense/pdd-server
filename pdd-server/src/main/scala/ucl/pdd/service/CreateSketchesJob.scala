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

import com.google.inject.{Inject, Singleton}
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import org.joda.time.Instant
import ucl.pdd.domain._
import ucl.pdd.storage.{ActivityStore, CampaignStore, Storage}
import ucl.pdd.strategy.GroupStrategy

import scala.util.Random

/**
 * Cron job creating each day the relevant sketches for the active campaigns.
 *
 * @param storage  Storage.
 * @param strategy Groups strategy.
 */
@Singleton
final class CreateSketchesJob @Inject()(storage: Storage, strategy: GroupStrategy)
  extends Job with Logging {

  override def execute(fireTime: Instant): Future[Unit] = {
    val f1 = storage.campaigns.list(CampaignStore.Query(isActive = Some(true)))

    // All clients are subscribed to all campaigns. We therefore retrieve once
    // and for all the list of all clients active during the past day.
    val endTime = fireTime.toDateTime.withTimeAtStartOfDay()
    val startTime = endTime.minusDays(1)
    val f2 = storage.activity.list(ActivityStore.Query(
      startTime = Some(startTime.toInstant),
      endTime = Some(endTime.toInstant)))

    Future.join(f1, f2).flatMap { case (campaigns, activity) =>
      val clientNames = activity.map(_.clientName).distinct
      Future.join(campaigns.map(handleCampaign(fireTime, _, clientNames)))
    }
  }

  private def handleCampaign(now: Instant, campaign: Campaign, clientNames: Seq[String]): Future[Unit] = {
    // On a given day `d`, we create the sketches for day `d - 1`.
    val currentDay = Campaign.relativeDay(campaign.startTime.get, now)
    val day = currentDay - 1
    if (day < 0) {
      info(s"Nothing to do for campaign ${campaign.name} (just started)")
      Future.Done
    } else {
      handleCampaign(now, day, campaign, clientNames)
    }
  }

  private def handleCampaign(now: Instant, day: Int, campaign: Campaign, clientNames: Seq[String]): Future[Unit] = {
    val sampledClientNames = sampleClients(clientNames, campaign)
    storage.clients.multiGet(sampledClientNames).flatMap { maybeClients =>
      val attrs = GroupStrategy.Attrs(
        groupSize = campaign.groupSize,
        startTime = campaign.startTime.get,
        delay = campaign.delay,
        graceDelay = campaign.graceDelay)

      val publicKeys = maybeClients.collect {
        case Some(client) => client.name -> client.publicKey
      }.toMap

      strategy.apply(publicKeys.keys, day, attrs)
        .flatMap { groups =>
          val fs = groups.flatMap { group =>
            group.clientNames.map { clientName =>
              val sketch = Sketch(
                name = UUID.randomUUID().toString,
                createTime = now,
                clientName = clientName,
                campaignName = campaign.name,
                group = group.id,
                submitted = false,
                queriesCount = campaign.vocabulary.queries.size,
                day = day,
                publicKey = publicKeys(clientName))
              storage.sketches.create(sketch).unit
            }
          }.toSeq
          Future.collect(fs)
        }
        .onSuccess { res =>
          info(s"Created ${res.size} sketches for campaign ${campaign.name} (day $day)")
        }
        .unit
    }
  }

  private def sampleClients(clientNames: Seq[String], campaign: Campaign): Seq[String] =
    campaign.samplingRate match {
      case None | Some(1d) => clientNames
      case Some(samplingRate) => clientNames.filter(_ => Random.nextDouble() < samplingRate)
    }
}
