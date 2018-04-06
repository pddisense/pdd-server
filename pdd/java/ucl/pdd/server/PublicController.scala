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
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.api._
import ucl.pdd.service.PingService
import ucl.pdd.storage.Storage

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
    storage.clients.delete(req.name).map {
      case false => response.notFound
      case true => response.ok
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
