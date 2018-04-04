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
import ucl.pdd.storage._

final class MysqlStorage(mysql: MysqlClient) extends Storage {
  override val clients = new MysqlClientStore(mysql)
  override val campaigns = new MysqlCampaignStore(mysql)
  override val aggregations = new MysqlAggregationStore(mysql)
  override val sketches = new MysqlSketchStore(mysql)
  override val activity = new MysqlActivityStore(mysql)

  override def startUp(): Future[Unit] = {
    val fs = MysqlStorage.Ddl.map(mysql.query(_).unit)
    Future.join(fs)
  }

  override def shutDown(): Future[Unit] = {
    mysql.close()
  }
}

object MysqlStorage {
  private val Ddl = Seq(
    "create table if not exists clients(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "createTime timestamp not null," +
      "publicKey varchar(255) not null," +
      "browser varchar(255) not null," +
      "externalName varchar(255) not null," +
      "leaveTime timestamp null," +
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8",

    "create table if not exists campaigns(" +
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
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8",

    "create table if not exists aggregations(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "campaignName varchar(255) not null," +
      "day int not null," +
      "decryptedValues text not null," +
      "rawValues text not null," +
      "activeCount int not null," +
      "submittedCount int not null," +
      "decryptedCount int not null," +
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8",

    "create table if not exists sketches(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "clientName varchar(255) not null," +
      "campaignName varchar(255) not null," +
      "`group` int not null," +
      "day int not null," +
      "publicKey varchar(255) not null," +
      "encryptedValues text not null," +
      "rawValues text not null," +
      "submitTime timestamp null," +
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8",

    "create table if not exists activity(" +
      "unused_id int not null auto_increment," +
      "clientName varchar(255) not null," +
      "time timestamp not null," +
      "countryCode varchar(255) not null," +
      "primary key (unused_id)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8")
}
