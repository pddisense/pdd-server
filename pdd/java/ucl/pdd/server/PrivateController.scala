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
      notes = req.notes,
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

  delete("/api/campaigns/:name") { req: DeleteCampaignRequest =>
    storage.campaigns.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(campaign) =>
        if (!campaign.isActive || req.force) {
          Future.join(Seq(
            storage.campaigns.delete(campaign.name),
            storage.aggregations.delete(AggregationStore.Query(campaign.name)),
            storage.sketches
              .list(SketchStore.Query(campaignName = Some(campaign.name)))
              .flatMap(sketches => Future.join(sketches.map(s => storage.sketches.delete(s.name))))))
            .map(_ => response.ok)
        } else {
          Future.value(response.badRequest(
            ValidationResult.Invalid(Seq(ErrorCause("Cannot delete an active campaign.")))))
        }
    }
  }

  get("/api/campaigns/:name/results") { req: GetResultsRequest =>
    storage.campaigns.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(campaign) =>
        storage
          .aggregations
          .list(AggregationStore.Query(campaignName = req.name))
          .map { aggregations =>
            if (req.export) {
              export(req.request, campaign, aggregations)
            } else {
              GetResultsResponse(aggregations.map(_.withoutValues))
            }
          }
    }
  }

  get("/api/campaigns/:name/results/:day") { req: GetResultRequest =>
    storage.campaigns.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(campaign) =>
        storage
          .aggregations
          .get(s"${req.name}-${req.day}")
          .map {
            case None => response.notFound
            case Some(aggregation) =>
              if (req.export) export(req.request, campaign, Seq(aggregation)) else aggregation
          }
    }
  }

  private def export(request: Request, campaign: Campaign, results: Seq[Aggregation]) = {
    RequestUtils.respondTo(request) {
      case ContentType.CSV =>
        val content = Exporter.csv(campaign, results)
        response.ok(content).contentType(ContentType.CSV.contentTypeName)
      case ContentType.JSON => response.ok(Exporter.json(campaign, results))
      case _ => response.notAcceptable
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
          // them have to be specified.
          displayName = req.displayName,
          email = req.email,
          notes = req.notes,
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
    val startTime = req.tail.map(n => DateTime.now().withTimeAtStartOfDay().minusDays(n - 1).toInstant)
    storage
      .activity
      .list(ActivityStore.Query(clientName = Some(req.name), startTime = startTime))
      .map(GetActivityResponse.apply)
  }

  get("/api/activity") { req: GetActivityRequest =>
    val startTime = req.tail.map(n => DateTime.now().withTimeAtStartOfDay().minusDays(n - 1).toInstant)
    storage
      .activity
      .list(ActivityStore.Query(startTime = startTime))
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
}

case class GetStatisticsResponse(activeCampaigns: Int, activeClients: Int)

case class GetCampaignRequest(@RouteParam name: String)

case class DeleteCampaignRequest(@RouteParam name: String, @QueryParam force: Boolean = false)

case class GetResultsRequest(
  @RouteParam name: String,
  @QueryParam export: Boolean = false,
  request: Request)

case class GetResultRequest(
  @RouteParam name: String,
  @RouteParam day: Int,
  @QueryParam export: Boolean = false,
  request: Request)

case class GetResultsResponse(results: Seq[Aggregation])

case class ListCampaignsRequest(@QueryParam active: Option[Boolean])

case class ListCampaignsResponse(campaigns: Seq[Campaign])

case class CreateCampaignRequest(
  displayName: String,
  email: Option[String],
  notes: Option[String],
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
  email: Option[String],
  notes: Option[String],
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
