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

import com.google.inject.{Inject, Singleton}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.storage.{CampaignStore, ClientStore, Storage}

@Singleton
final class AdminController @Inject()(storage: Storage) extends Controller {
  get("/api/campaigns") { req: ListCampaignsRequest =>
    storage
      .campaigns
      .list(CampaignStore.Query(isActive = req.active))
      .map(campaigns => ObjectList(campaigns.map(_.withoutVocabulary)))
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

  get("/api/campaigns/:id") { req: GetCampaignRequest =>
    storage.campaigns.get(req.id)
  }

  put("/api/campaigns/:id") { req: UpdateCampaignRequest =>
    storage.campaigns.get(req.id).flatMap {
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

  get("/api/clients") { req: ListClientsRequest =>
    storage
      .clients
      .list(ClientStore.Query(hasLeft = req.active))
      .map(clients => ObjectList(clients))
  }

  get("/api/clients/:name") { req: GetClientRequest =>
    storage.clients.get(req.name)
  }
}

case class GetCampaignRequest(@RouteParam id: String)

case class ListCampaignsRequest(@QueryParam active: Option[Boolean])

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
  @RouteParam id: String,
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

case class ListClientsRequest(@QueryParam active: Option[Boolean])

case class GetClientRequest(@RouteParam name: String)
