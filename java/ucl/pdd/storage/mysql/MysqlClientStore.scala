package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Client
import ucl.pdd.storage.{ClientQuery, ClientStore}

private[mysql] final class MysqlClientStore(mysql: MysqlClient) extends ClientStore {
  override def save(client: Client): Future[Unit] = ???

  override def list(query: ClientQuery): Future[Seq[Client]] = ???

  override def get(name: String): Future[Option[Client]] = ???
}
