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

import com.google.inject.{Inject, Singleton}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import com.twitter.util.Future
import org.joda.time.Instant
import ucl.pdd.storage.Storage

@Singleton
final class SketchController @Inject()(storage: Storage) extends Controller {
  put("/api/sketches/:name") { req: UpdateSketchRequest =>
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
          storage
            .sketches
            .replace(updated)
            .map {
              case true => response.ok
              case false => response.notFound
            }
      }
  }
}

case class UpdateSketchRequest(
  @RouteParam name: String,
  encryptedValues: Option[Seq[String]],
  rawValues: Option[Seq[Long]])
