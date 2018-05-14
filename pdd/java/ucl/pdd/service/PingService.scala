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
import com.google.inject.{Inject, Singleton}
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.domain._
import ucl.pdd.storage.{SketchStore, Storage}

import scala.util.Random

/**
 * The ping service is regularly called by clients to get their instructions. It should typically
 * be called once a day (assuming that clients actually come online), but it may happen that it is
 * called more often (e.g., if it did not respond the first time or when the browser extension is
 * updated).
 *
 * This endpoint returns a [[PingResponse]], which essentially contains two pieces of information:
 * a list of sketches to compute and send back to the server, and when to perform the next ping.
 * Would the need arise, we could perfectly pre-build all responses in advance and cache them,
 * instead of recreating them every time this service is called. However, because we expect the
 * load to be low (except if somebody tries a DDoS...), it is simpler like this for now.
 *
 * @param storage     Persistent storage.
 * @param timezone    Reference timezone.
 * @param testingMode Whether we are in testing mode, where days are shorter.
 */
@Singleton
final class PingService @Inject()(
  storage: Storage,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean) {

  def apply(clientName: String, now: Instant): Future[Option[PingResponse]] = {
    storage.clients.get(clientName).flatMap {
      case None => Future.value(None)
      case Some(client) =>
        Future
          .join(recordActivity(client, now), createResponse(client, now))
          .map { case (_, resp) => Some(resp) }
    }
  }

  private def recordActivity(client: Client, now: Instant): Future[Unit] = {
    // We keep a log of times at which clients send their pings. This is useful both for
    // debugging purposes, and to the algorithm in charge of creating groups. The latter may
    // use historical activity to optimize groups (i.e., by pruning long-inactive users).
    storage.activity.create(Activity(client.name, now, None))
  }

  private def createResponse(client: Client, now: Instant): Future[PingResponse] = {
    // Here happens the main work: for each non-submitted sketch of this client, a command is
    // generated instructing the client what to do.
    storage.sketches
      .list(SketchStore.Query(clientName = Some(client.name), submitted = Some(false)))
      .flatMap { sketches =>
        // Small optimization: we retrieve once all involved campaigns, instead of retrieving
        // them for each sketch. Indeed, there may be multiple sketches about the same campaign
        // in `sketches`.
        batchGetCampaigns(sketches.map(_.campaignName))
          .flatMap { campaigns =>
            val fs = sketches.map(sketch => createCommand(campaigns(sketch.campaignName), client, sketch))
            Future.collect(fs)
          }
      }
      .map { commands =>
        val nextPingTime = if (testingMode) {
          now.plus(Duration.standardMinutes(5).millis)
        } else {
          // The sketches are generated at 1:00, so we ask the clients to contact the server
          // between 2:00 and 3:00 to get their instructions.
          //
          // We add some randomness to avoid all clients contacting the server at the same time.
          // Although people are expected to be sleeping at 2:00, their computer might still be
          // on and we prefer to avoid all clients sending their ping at the exact same moment.
          now.toDateTime(timezone)
            .plusDays(1)
            .withTimeAtStartOfDay
            .plusHours(2)
            .plusMinutes(Random.nextInt(60)) // <- Randomness.
        }
        PingResponse(commands, Some(nextPingTime.toInstant))
      }
  }

  private def batchGetCampaigns(ids: Seq[String]): Future[Map[String, Campaign]] = {
    storage.campaigns
      .batchGet(ids)
      .map(_.flatMap(_.toSeq).map(campaign => campaign.name -> campaign).toMap)
  }

  private def createCommand(campaign: Campaign, client: Client, sketch: Sketch): Future[PingResponse.Command] = {
    collectGroupKeys(client.name, sketch.campaignName, sketch.day, sketch.group)
      .map { publicKeys =>
        // Note: If a campaign has some sketches, its `startTime` is defined (otherwise there is
        // an invariant not checked somewhere else).
        val (startTime, endTime) = if (testingMode) {
          val startTime = campaign.startTime.get.toDateTime(timezone) + (sketch.day * 5).minutes
          val endTime = startTime + 5.minutes
          (startTime, endTime)
        } else {
          val startTime = campaign.startTime.get.toDateTime(timezone).withTimeAtStartOfDay + sketch.day.days
          val endTime = startTime + 1.day
          (startTime, endTime)
        }
        PingResponse.Command(
          sketchName = sketch.name,
          startTime = startTime.toInstant,
          endTime = endTime.toInstant,
          vocabulary = campaign.vocabulary.take(sketch.queriesCount),
          publicKeys = publicKeys,
          collectRaw = campaign.collectRaw,
          collectEncrypted = campaign.collectEncrypted,
          round = sketch.day)
      }
  }

  private def collectGroupKeys(clientName: String, campaignName: String, day: Int, group: Int): Future[Seq[String]] = {
    storage.sketches
      .list(SketchStore.Query(campaignName = Some(campaignName), day = Some(day), group = Some(group)))
      .map { sketches =>
        // It is important that the group keys are indeed sorted, because the encryption routine
        // relies on the position of the key in the list.
        sketches.sortBy(_.clientName).map(sketch => sketch.publicKey)
      }
  }
}
