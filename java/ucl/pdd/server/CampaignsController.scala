package ucl.pdd.server

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.storage.{CampaignQuery, Storage}

@Singleton
final class CampaignsController @Inject()(storage: Storage) extends Controller {
  get("/api/campaigns") { req: ListCampaignsRequest =>
    storage
      .campaigns
      .list(CampaignQuery(isActive = req.active))
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
      case ValidationResult.Valid => storage.campaigns.save(campaign).map(_ => campaign)
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
          case ValidationResult.Valid => storage.campaigns.save(campaign).map(_ => campaign)
          case err: ValidationResult.Invalid => Future.value(response.badRequest(err))
        }
    }
  }
}

case class GetCampaignRequest(@RouteParam id: String)

case class ListCampaignsRequest(@QueryParam active: Option[Boolean])

case class CreateCampaignRequest(
  displayName: Option[String] = None,
  email: Seq[String] = Seq.empty,
  vocabulary: Vocabulary = Vocabulary(),
  startTime: Option[Instant] = None,
  endTime: Option[Instant] = None,
  collectRaw: Boolean = true,
  collectEncrypted: Boolean = true,
  delay: Int = 0,
  graceDelay: Int = 0,
  groupSize: Int = 10,
  samplingRate: Option[Double])

case class UpdateCampaignRequest(
  @RouteParam id: String,
  displayName: Option[String],
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
