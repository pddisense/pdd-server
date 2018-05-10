package ucl.pdd.storage.memory

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import ucl.pdd.api.{Client, instantOrdering}
import ucl.pdd.storage.{ClientQuery, ClientStore}

import scala.collection.JavaConverters._

private[memory] final class MemoryClientStore extends ClientStore {
  private[this] val index = new ConcurrentHashMap[String, Client]().asScala

  override def save(client: Client): Future[Unit] = {
    index(client.name) = client
    Future.Done
  }

  override def list(query: ClientQuery): Future[Seq[Client]] = {
    Future.value(index.values.filter(query.matches).toSeq.sortBy(_.createTime).reverse)
  }

  override def get(name: String): Future[Option[Client]] = Future.value(index.get(name))
}
