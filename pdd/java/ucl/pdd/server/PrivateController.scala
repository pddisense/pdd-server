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
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.request.RequestUtils
import com.twitter.finatra.request.{ContentType, QueryParam, RouteParam}
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.storage._

@Singleton
final class PrivateController @Inject()(storage: Storage) extends Controller {
  get("/api/campaigns") { req: ListCampaignsRequest =>
    storage
      .campaigns
      .list(CampaignStore.Query(isActive = req.active))
      .map(campaigns => ListCampaignsResponse(campaigns.map(_.withoutVocabulary)))
  }

  post("/api/campaigns") { req: CreateCampaignRequest =>
    val campaign = Campaign(
      // `name` and `createTime` are automatically filled.
      name = UUID.randomUUID().toString,
      createTime = Instant.now(),
      // Other fields come from the request. They are all optional, a default will be used if they
      // are left empty.
      displayName = req.displayName,
      email = req.email,
      vocabulary = req.vocabulary,
      startTime = req.startTime,
      endTime = req.endTime,
      collectRaw = req.collectRaw,
      collectEncrypted = req.collectEncrypted,
      delay = req.delay,
      graceDelay = req.graceDelay,
      groupSize = req.groupSize,
      samplingRate = req.samplingRate)
    CampaignValidator.validate(campaign) match {
      case ValidationResult.Valid =>
        storage
          .campaigns
          .create(campaign)
          .map {
            case true => response.ok(campaign)
            case false => response.conflict
          }
      case err: ValidationResult.Invalid => response.badRequest(err)
    }
  }

  get("/api/campaigns/:name") { req: GetCampaignRequest =>
    storage.campaigns.get(req.name)
  }

  get("/api/campaigns/:name/results") { req: GetResultsRequest =>
    storage
      .aggregations
      .list(AggregationStore.Query(campaignName = req.name))
      .map { aggregations =>
        RequestUtils.respondTo(req.request) {
          case ContentType.CSV =>
            val header = "day,query,decrypted,count"
            val content = aggregations.flatMap { agg =>
              agg.rawValues.zipWithIndex.map { case (v, idx) => s"${agg.day},$idx,0,$v" } ++
                agg.decryptedValues.zipWithIndex.map { case (v, idx) => s"${agg.day},$idx,1,$v" }
            }
            response
              .ok((header +: content).mkString("\n"))
              .contentType("text/csv")
          case _ => GetResultsResponse(aggregations.map(_.withoutValues))
        }
      }
  }

  put("/api/campaigns/:name") { req: UpdateCampaignRequest =>
    storage.campaigns.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(previous) =>
        val campaign = Campaign(
          // `name` and `createTime` are always immutable.
          name = previous.name,
          createTime = previous.createTime,
          // Other fields come from the request. As opposed to when a campaign is created, all of
          // them have to been specified.
          displayName = req.displayName,
          email = req.email,
          vocabulary = req.vocabulary,
          startTime = req.startTime,
          endTime = req.endTime,
          collectRaw = req.collectRaw,
          collectEncrypted = req.collectEncrypted,
          delay = req.delay,
          graceDelay = req.graceDelay,
          groupSize = req.groupSize,
          samplingRate = req.samplingRate)
        CampaignValidator.validate(campaign) match {
          case ValidationResult.Valid =>
            storage.campaigns.replace(campaign).map {
              case true => response.ok(campaign)
              case false => response.notFound
            }
          case err: ValidationResult.Invalid => Future.value(response.badRequest(err))
        }
    }
  }

  get("/api/clients") { req: Request =>
    storage.clients.list().map(clients => ListClientsResponse(clients))
  }

  get("/api/clients/:name") { req: GetClientRequest =>
    storage.clients.get(req.name)
  }

  get("/api/clients/:name/activity") { req: GetClientActivityRequest =>
    val (startTime, endTime) = req.tail match {
      case None => (None, None)
      case Some(n) =>
        val endTime = DateTime.now().withTimeAtStartOfDay()
        val startTime = endTime.minusDays(n)
        (Some(startTime.toInstant), Some(endTime.toInstant.minus(1)))
    }
    storage
      .activity
      .list(ActivityStore.Query(clientName = Some(req.name), startTime = startTime, endTime = endTime))
      .map(GetActivityResponse.apply)
  }

  get("/api/activity") { req: GetActivityRequest =>
    val (startTime, endTime) = req.tail match {
      case None => (None, None)
      case Some(n) =>
        val endTime = DateTime.now().withTimeAtStartOfDay()
        val startTime = endTime.minusDays(n)
        (Some(startTime.toInstant), Some(endTime.toInstant.minus(1)))
    }
    storage
      .activity
      .list(ActivityStore.Query(startTime = startTime, endTime = endTime))
      .map(GetActivityResponse.apply)
  }

  get("/api/stats") { _: Request =>
    val f1 = storage.campaigns.count(CampaignStore.Query(isActive = Some(true)))
    val f2 = storage.clients.count()
    Future
      .join(f1, f2)
      .map { case (activeCampaigns, activeClients) =>
        GetStatisticsResponse(activeCampaigns = activeCampaigns, activeClients = activeClients)
      }
  }

  get("/api/results/:name") { req: GetResultRequest =>
    storage
      .aggregations
      .get(req.name)
      .map {
        case None => response.notFound
        case Some(aggregation) => aggregation
      }
  }
}

case class GetStatisticsResponse(activeCampaigns: Int, activeClients: Int)

case class GetCampaignRequest(@RouteParam name: String)

case class GetResultsRequest(@RouteParam name: String, request: Request)

case class GetResultRequest(
  @RouteParam name: String,
  @QueryParam download: Boolean = false)

case class GetResultsResponse(results: Seq[Aggregation])

case class ListCampaignsRequest(@QueryParam active: Option[Boolean])

case class ListCampaignsResponse(campaigns: Seq[Campaign])

case class CreateCampaignRequest(
  displayName: String,
  email: Seq[String] = Seq.empty,
  vocabulary: Vocabulary = Vocabulary(),
  startTime: Option[Instant],
  endTime: Option[Instant],
  collectRaw: Boolean = true,
  collectEncrypted: Boolean = true,
  delay: Int = 0,
  graceDelay: Int = 0,
  groupSize: Int = 10,
  samplingRate: Option[Double])

case class UpdateCampaignRequest(
  @RouteParam name: String,
  displayName: String,
  email: Seq[String],
  vocabulary: Vocabulary,
  startTime: Option[Instant],
  endTime: Option[Instant],
  collectRaw: Boolean,
  collectEncrypted: Boolean,
  delay: Int,
  graceDelay: Int,
  groupSize: Int,
  samplingRate: Option[Double])

case class GetClientRequest(@RouteParam name: String)

case class GetClientActivityRequest(@RouteParam name: String, @QueryParam tail: Option[Int])

case class GetActivityRequest(@QueryParam tail: Option[Int])

case class GetActivityResponse(days: Seq[Activity])

case class ListClientsResponse(clients: Seq[Client])

case class GetAggregationRequest(@RouteParam name: String)
