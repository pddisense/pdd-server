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

package ucl.pdd.server

import java.util.UUID

import com.github.nscala_time.time.Imports._
import com.google.inject.{Inject, Singleton}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.config.{TestingMode, Timezone}
import ucl.pdd.storage.{SketchStore, Storage}

@Singleton
final class PublicController @Inject()(
  storage: Storage,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean)
  extends Controller {

  post("/api/clients") { req: CreateClientRequest =>
    val client = Client(
      name = UUID.randomUUID().toString,
      createTime = Instant.now(),
      browser = req.browser,
      publicKey = req.publicKey,
      externalName = req.externalName)
    ClientValidator.validate(client) match {
      case ValidationResult.Valid =>
        storage.clients.create(client).map {
          case true => response.ok(client)
          case false => response.conflict
        }
      case err: ValidationResult.Invalid => response.badRequest(err)
    }
  }

  get("/api/clients/:name/ping") { req: PingClientRequest =>
    storage.clients.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(client) =>
        storage
          .sketches
          .list(SketchStore.Query(clientName = Some(client.name), isSubmitted = Some(false)))
          .flatMap { sketches =>
            batchGetCampaigns(sketches.map(_.campaignName))
              .flatMap { campaigns =>
                val fs = sketches.map { sketch =>
                  val campaign = campaigns(sketch.campaignName)
                  collectKeys(client.name, sketch.campaignName, sketch.group)
                    .map { publicKeys =>
                      // Note: If a campaign is active, its `startTime` is defined.
                      val startTime = if (testingMode) {
                        campaign.startTime.get.toDateTime(timezone) + (sketch.day * 5).minutes
                      } else {
                        campaign.startTime.get.toDateTime(timezone).withTimeAtStartOfDay + sketch.day.days
                      }
                      val endTime = startTime + 1.day
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
              DateTime.now(timezone).plusMinutes(5)
            } else {
              DateTime.now(timezone).plusDays(1).withTimeAtStartOfDay.plusHours(1)
            }
            PingResponse(submit, Some(nextPingTime.toInstant))
          }
    }
  }

  delete("/api/clients/:name") { req: DeleteClientRequest =>
    storage.clients.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(client) =>
        val updated = client.copy(leaveTime = Some(Instant.now))
        storage.clients.replace(updated).map {
          case true => response.ok
          case false => response.notFound
        }
    }
  }

  patch("/api/sketches/:name") { req: UpdateSketchRequest =>
    // In practice this corresponds to a JSON merge patch.
    storage
      .sketches
      .get(req.name)
      .flatMap {
        case None => Future.value(response.notFound)
        case Some(sketch) =>
          val updated = sketch.copy(
            submitTime = Some(Instant.now()),
            encryptedValues = req.encryptedValues,
            rawValues = req.rawValues)
          storage.sketches.replace(updated).map {
            case true => response.ok
            case false => response.notFound
          }
      }
  }

  private def batchGetCampaigns(ids: Seq[String]): Future[Map[String, Campaign]] = {
    storage
      .campaigns
      .batchGet(ids)
      .map(_.flatMap(_.toSeq).map(campaign => campaign.name -> campaign).toMap)
  }

  private def collectKeys(clientName: String, campaignName: String, group: Int): Future[Seq[String]] = {
    storage
      .sketches
      .list(SketchStore.Query(campaignName = Some(campaignName), group = Some(group)))
      .map(sketches => sketches.sortBy(_.clientName).map(sketch => sketch.publicKey))
  }
}

case class PingClientRequest(@RouteParam name: String)

case class CreateClientRequest(
  publicKey: String,
  browser: String,
  externalName: Option[String])

case class DeleteClientRequest(@RouteParam name: String)

case class UpdateSketchRequest(
  @RouteParam name: String,
  encryptedValues: Option[Seq[String]],
  rawValues: Option[Seq[Long]])
