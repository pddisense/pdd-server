package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import ucl.pdd.api.Sketch
import ucl.pdd.storage.{SketchQuery, SketchStore}

import scala.collection.JavaConverters._

private[memory] final class MemorySketchStore extends SketchStore {
  private[this] val index = new ConcurrentHashMap[String, Sketch]().asScala

  override def save(sketch: Sketch): Future[Unit] = {
    index(sketch.name) = sketch
    Future.Done
  }

  override def delete(name: String): Future[Unit] = {
    index.remove(name)
    Future.Done
  }

  override def get(name: String): Future[Option[Sketch]] = {
    Future.value(index.get(name))
  }

  override def list(query: SketchQuery = SketchQuery()): Future[Seq[Sketch]] = {
    Future.value(index.values.filter(query.matches).toSeq)
  }
}
