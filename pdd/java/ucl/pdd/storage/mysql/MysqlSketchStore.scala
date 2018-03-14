/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
