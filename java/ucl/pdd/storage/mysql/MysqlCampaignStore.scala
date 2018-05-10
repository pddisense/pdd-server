package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Campaign
import ucl.pdd.storage.{CampaignQuery, CampaignStore}

private[mysql] final class MysqlCampaignStore(mysql: MysqlClient) extends CampaignStore {
  override def save(campaign: Campaign): Future[Unit] = ???

  override def delete(id: String): Future[Unit] = ???

  override def list(query: CampaignQuery): Future[Seq[Campaign]] = ???

  override def get(id: String): Future[Option[Campaign]] = ???
}
