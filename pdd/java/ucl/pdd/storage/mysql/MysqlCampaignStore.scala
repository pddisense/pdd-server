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
import ucl.pdd.api.Campaign
import ucl.pdd.storage.{CampaignQuery, CampaignStore}

private[mysql] final class MysqlCampaignStore(mysql: MysqlClient) extends CampaignStore {
  override def list(query: CampaignQuery): Future[Seq[Campaign]] = ???

  override def get(id: String): Future[Option[Campaign]] = ???

  override def create(campaign: Campaign): Future[Boolean] = ???

  override def replace(campaign: Campaign): Future[Boolean] = ???
}

private[mysql] object MysqlCampaignStore {
  val CreateSchemaDDL = Map(
    "campaigns" -> ("create table campaigns(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "createTime timestamp not null," +
      "displayName varchar(255) not null," +
      "email text not null," +
      "vocabulary text not null," +
      "startTime timestamp null," +
      "endTime timestamp null," +
      "collectRaw tinyint," +
      "collectEncrypted tinyint," +
      "delay int," +
      "graceDelay int," +
      "groupSize int," +
      "samplingRate double," +
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8"))
}
