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

import com.google.inject.{Inject, Singleton}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.service.PingService
import ucl.pdd.storage.{ActivityStore, Storage}

@Singleton
final class PublicController @Inject()(storage: Storage, pingService: PingService)
  extends Controller {

  options("/api/:*") { _: Request => response.ok }

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

  patch("/api/clients/:name") { req: UpdateClientRequest =>
    storage.clients.get(req.name).flatMap {
      case None => Future.value(response.notFound)
      case Some(previous) =>
        val client = previous.copy(externalName = req.externalName)
        ClientValidator.validate(client) match {
          case ValidationResult.Valid =>
            storage.clients.replace(client).map {
              case true => response.ok(client)
              case false => response.notFound
            }
          case err: ValidationResult.Invalid => Future.value(response.badRequest(err))
        }
    }
  }

  get("/api/clients/:name/ping") { req: PingClientRequest =>
    pingService.apply(req.name, Instant.now).map {
      case None => response.notFound
      case Some(resp) => resp
    }
  }

  delete("/api/clients/:name") { req: DeleteClientRequest =>
    storage.clients.delete(req.name).flatMap {
      case false => Future.value(response.notFound)
      case true =>
        storage
          .activity
          .delete(ActivityStore.Query(clientName = Some(req.name)))
          .map(_ => response.ok)
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
            submitted = true,
            encryptedValues = req.encryptedValues,
            rawValues = req.rawValues)
          storage.sketches.replace(updated).map {
            case true => response.ok
            case false => response.notFound
          }
      }
  }
}

case class PingClientRequest(@RouteParam name: String)

case class CreateClientRequest(
  publicKey: String,
  browser: String,
  externalName: Option[String])

case class UpdateClientRequest(
  @RouteParam name: String,
  externalName: Option[String])

case class DeleteClientRequest(@RouteParam name: String)

case class UpdateSketchRequest(
  @RouteParam name: String,
  encryptedValues: Option[Seq[String]],
  rawValues: Option[Seq[Long]])
