package ucl.pdd.server

import java.util.UUID

import com.github.nscala_time.time.Imports._
import com.google.inject.{Inject, Singleton}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.config.DayDuration
import ucl.pdd.storage.{ClientQuery, SketchQuery, Storage}

@Singleton
final class ClientsController @Inject()(storage: Storage, @DayDuration dayDuration: Duration)
  extends Controller {

  get("/api/clients") { req: ListClientsRequest =>
    storage
      .clients
      .list(ClientQuery(hasLeft = req.active))
      .map(clients => ObjectList(clients))
  }

  post("/api/clients") { req: CreateClientRequest =>
    val client = Client(
      name = UUID.randomUUID().toString,
      createTime = Instant.now(),
      browser = req.browser,
      publicKey = req.publicKey,
      externalName = req.externalName)
    ClientValidator.validate(client) match {
      case ValidationResult.Valid => storage.clients.save(client).map(_ => client)
      case err: ValidationResult.Invalid => response.badRequest(err)
    }
  }

  get("/api/clients/:name") { req: GetClientRequest =>
    storage.clients.get(req.name)
  }

  get("/api/clients/:name/ping") { req: PingClientRequest =>
    storage.clients.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(client) =>
        storage
          .sketches
          .list(SketchQuery(clientName = Some(client.name), isSubmitted = Some(false)))
          .flatMap { sketches =>
            batchGetCampaigns(sketches.map(_.campaignName))
              .flatMap { campaigns =>
                val fs = sketches.map { sketch =>
                  val campaign = campaigns(sketch.campaignName)
                  collectKeys(client.name, sketch.campaignName, sketch.group)
                    .map { publicKeys =>
                      val startTime = campaign.startTime.get + (dayDuration * sketch.day)
                      val endTime = startTime + dayDuration
                      SubmitSketchCommand(
                        name = sketch.name,
                        startTime = startTime,
                        endTime = endTime,
                        vocabulary = Some(campaign.vocabulary),
                        publicKeys = publicKeys,
                        collectRaw = campaign.collectRaw,
                        collectEncrypted = campaign.collectEncrypted)
                    }
                }
                Future.collect(fs)
              }
          }
          .map { submit =>
            val now = DateTime.now()
            val toNextDay = dayDuration.millis - (now.getMillis % dayDuration.millis)
            val nextPingTime = (now + toNextDay).toInstant
            PingResponse(submit, Some(nextPingTime))
          }
    }
  }

  delete("/api/clients/:name") { req: DeleteClientRequest =>
    storage.clients.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(client) =>
        val updated = client.copy(leaveTime = Some(Instant.now))
        storage.clients.save(updated).map(_ => updated)
    }
  }

  private def batchGetCampaigns(ids: Seq[String]): Future[Map[String, Campaign]] = {
    storage
      .campaigns
      .batchGet(ids)
      .map(_.flatMap(_.toSeq).map(campaign => campaign.name -> campaign).toMap)
  }

  private def collectKeys(clientName: String, campaignName: String, group: Int): Future[Seq[ClientKey]] = {
    storage
      .sketches
      .list(SketchQuery(campaignName = Some(campaignName), group = Some(group)))
      .map { sketches =>
        sketches
          .sortBy(_.clientName)
          .map(sketch => ClientKey(sketch.publicKey, sketch.clientName == clientName))
      }
  }
}

case class GetClientRequest(@RouteParam name: String)

case class PingClientRequest(@RouteParam name: String)

case class ListClientsRequest(@QueryParam active: Option[Boolean])

case class CreateClientRequest(
  publicKey: String,
  browser: String,
  externalName: Option[String])

case class DeleteClientRequest(@RouteParam name: String)
