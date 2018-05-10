package ucl.pdd.storage.mysql

import com.google.common.util.concurrent.AbstractIdleService
import com.twitter.finagle.mysql.{Client => MysqlClient}
import ucl.pdd.storage._

final class MysqlStorage(mysql: MysqlClient) extends AbstractIdleService with Storage {
  override val clients = new MysqlClientStore(mysql)

  override val campaigns = new MysqlCampaignStore(mysql)

  override val aggregations = new MysqlAggregationStore(mysql)

  override val sketches = new MysqlSketchStore(mysql)

  override def shutDown(): Unit = {}

  override def startUp(): Unit = {}
}
