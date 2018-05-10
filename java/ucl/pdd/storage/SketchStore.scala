package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Sketch

trait SketchStore {
  def save(sketch: Sketch): Future[Unit]

  def delete(name: String): Future[Unit]

  def get(name: String): Future[Option[Sketch]]

  def list(query: SketchQuery = SketchQuery()): Future[Seq[Sketch]]
}

case class SketchQuery(
  clientName: Option[String] = None,
  campaignName: Option[String] = None,
  group: Option[Int] = None,
  day: Option[Int] = None,
  isSubmitted: Option[Boolean] = None) {

  def matches(sketch: Sketch): Boolean = {
    clientName.forall(sketch.clientName == _) &&
      campaignName.forall(sketch.campaignName == _) &&
      group.forall(sketch.group == _) &&
      day.forall(sketch.day == _) &&
      isSubmitted.forall(sketch.isSubmitted == _)
  }
}
