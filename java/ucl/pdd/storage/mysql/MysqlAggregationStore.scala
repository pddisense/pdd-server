package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Aggregation
import ucl.pdd.storage.{AggregationQuery, AggregationStore}

private[mysql] final class MysqlAggregationStore(mysql: MysqlClient) extends AggregationStore {
  override def save(aggregation: Aggregation): Future[Unit] = ???

  override def list(query: AggregationQuery): Future[Seq[Aggregation]] = ???
}
