package ucl.pdd.storage.mysql

import com.twitter.finagle.mysql.{Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.Sketch
import ucl.pdd.storage.{SketchQuery, SketchStore}

private[mysql] final class MysqlSketchStore(mysql: MysqlClient) extends SketchStore {
  override def save(sketch: Sketch): Future[Unit] = ???

  override def delete(name: String): Future[Unit] = ???

  override def get(name: String): Future[Option[Sketch]] = ???

  override def list(query: SketchQuery): Future[Seq[Sketch]] = ???
}
