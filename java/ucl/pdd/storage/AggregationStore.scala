package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Aggregation

trait AggregationStore {
  def save(aggregation: Aggregation): Future[Unit]

  def list(query: AggregationQuery): Future[Seq[Aggregation]]
}

case class AggregationQuery(campaignName: String) {
  def matches(aggregation: Aggregation): Boolean = {
    aggregation.campaignName == campaignName
  }
}
