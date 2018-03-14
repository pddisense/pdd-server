/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
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
