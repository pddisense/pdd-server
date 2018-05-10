package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Campaign

trait CampaignStore {
  def save(campaign: Campaign): Future[Unit]

  def delete(name: String): Future[Unit]

  def list(query: CampaignQuery = CampaignQuery()): Future[Seq[Campaign]]

  def get(name: String): Future[Option[Campaign]]

  def batchGet(names: Seq[String]): Future[Seq[Option[Campaign]]] = Future.collect(names.map(get))
}

case class CampaignQuery(isActive: Option[Boolean] = None) {
  def matches(campaign: Campaign): Boolean = {
    isActive.forall(campaign.isActive == _)
  }
}
