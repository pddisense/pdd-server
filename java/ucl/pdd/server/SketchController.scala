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
          storage.sketches.save(updated).map(_ => response.ok)
      }
  }
}

case class UpdateSketchRequest(
  @RouteParam name: String,
  encryptedValues: Option[Seq[String]],
  rawValues: Option[Seq[Long]])
