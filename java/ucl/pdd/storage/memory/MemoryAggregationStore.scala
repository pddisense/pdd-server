package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import ucl.pdd.api.Aggregation
import ucl.pdd.storage.{AggregationQuery, AggregationStore}

import scala.collection.JavaConverters._

private[memory] final class MemoryAggregationStore extends AggregationStore {
  private[this] val index = new ConcurrentHashMap[String, Aggregation]().asScala

  override def save(aggregation: Aggregation): Future[Unit] = {
    index(aggregation.name) = aggregation
    Future.Done
  }

  override def list(query: AggregationQuery): Future[Seq[Aggregation]] = {
    Future.value(index.values.filter(query.matches).toSeq.sortBy(_.day).reverse)
  }
}
