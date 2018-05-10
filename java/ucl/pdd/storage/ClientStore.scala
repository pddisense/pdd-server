package ucl.pdd.storage

import com.twitter.util.Future
import ucl.pdd.api.Client

trait ClientStore {
  def save(client: Client): Future[Unit]

  def list(query: ClientQuery = ClientQuery()): Future[Seq[Client]]

  def get(name: String): Future[Option[Client]]
}

case class ClientQuery(hasLeft: Option[Boolean] = None) {
  def matches(client: Client): Boolean = {
    hasLeft.forall(client.hasLeft == _)
  }
}
