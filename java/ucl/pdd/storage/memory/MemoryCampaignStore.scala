package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import ucl.pdd.api.{Campaign, instantOrdering}
import ucl.pdd.storage.{CampaignQuery, CampaignStore}

import scala.collection.JavaConverters._

private[memory] final class MemoryCampaignStore extends CampaignStore {
  private[this] val index = new ConcurrentHashMap[String, Campaign]().asScala

  override def save(campaign: Campaign): Future[Unit] = {
    index(campaign.name) = campaign
    Future.Done
  }

  override def delete(name: String): Future[Unit] = {
    index.remove(name)
    Future.Done
  }

  override def list(query: CampaignQuery): Future[Seq[Campaign]] = {
    Future.value(index.values.filter(query.matches).toSeq.sortBy(_.createTime).reverse)
  }

  override def get(name: String): Future[Option[Campaign]] = Future.value(index.get(name))
}
