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
import ucl.pdd.api._
import ucl.pdd.storage.{SketchStore, Storage}

import scala.util.Random

@Singleton
final class PingService @Inject()(
  storage: Storage,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean) {

  def apply(clientName: String, now: Instant): Future[Option[PingResponse]] = {
    storage.clients.get(clientName).flatMap {
      case None => Future.value(None)
      case Some(client) =>
        storage.activity.create(Activity(clientName, now, None))
        storage
          .sketches
          .list(SketchStore.Query(clientName = Some(client.name), submitted = Some(false)))
          .flatMap { sketches =>
            batchGetCampaigns(sketches.map(_.campaignName))
              .flatMap { campaigns =>
                val fs = sketches.map { sketch =>
                  val campaign = campaigns(sketch.campaignName)
                  collectKeys(client.name, sketch.campaignName, sketch.day, sketch.group)
                    .map { publicKeys =>
                      // Note: If a campaign is active, its `startTime` is defined.
                      val (startTime, endTime) = if (testingMode) {
                        val startTime = campaign.startTime.get.toDateTime(timezone) + (sketch.day * 5).minutes
                        val endTime = startTime + 5.minutes
                        (startTime, endTime)
                      } else {
                        val startTime = campaign.startTime.get.toDateTime(timezone).withTimeAtStartOfDay + sketch.day.days
                        val endTime = startTime + 1.day
                        (startTime, endTime)
                      }
                      val round = sketch.day
                      SubmitSketchCommand(
                        sketchName = sketch.name,
                        startTime = startTime.toInstant,
                        endTime = endTime.toInstant,
                        vocabulary = Some(campaign.vocabulary),
                        publicKeys = publicKeys,
                        collectRaw = campaign.collectRaw,
                        collectEncrypted = campaign.collectEncrypted,
                        round = round)
                    }
                }
                Future.collect(fs)
              }
          }
          .map { submit =>
            val nextPingTime = if (testingMode) {
              now.plus(Duration.standardMinutes(5).millis)
            } else {
              // The sketches are generated at 1:00, so we ask the clients to contact the server
              // between 2:00 and 3:00 to get their instructions.
              // We add some randomness to avoid all clients contacting the server at the same time.
              // People are expected to be sleeping at 2:00, but their computer might still be on.
              now.toDateTime(timezone).plusDays(1).withTimeAtStartOfDay.plusHours(2).plusMinutes(Random.nextInt(60))
            }
            Some(PingResponse(submit, Some(nextPingTime.toInstant)))
          }
    }
  }

  private def batchGetCampaigns(ids: Seq[String]): Future[Map[String, Campaign]] = {
    storage
      .campaigns
      .batchGet(ids)
      .map(_.flatMap(_.toSeq).map(campaign => campaign.name -> campaign).toMap)
  }

  private def collectKeys(clientName: String, campaignName: String, day: Int, group: Int): Future[Seq[String]] = {
    storage
      .sketches
      .list(SketchStore.Query(campaignName = Some(campaignName), day = Some(day), group = Some(group)))
      .map(sketches => sketches.sortBy(_.clientName).map(sketch => sketch.publicKey))
  }
}
