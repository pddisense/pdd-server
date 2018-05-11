/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
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
      "primary key (unused_id)," +
      "UNIQUE KEY uix_name(name)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8",

    "create table if not exists campaigns(" +
      "unused_id int not null auto_increment," +
      "name varchar(255) not null," +
      "createTime timestamp not null," +
      "displayName varchar(255) not null," +
      "email text not null," +
      "notes text not null," +
      "vocabulary longtext not null," +
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
      "decryptedValues longtext not null," +
      "rawValues longtext not null," +
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
      "encryptedValues longtext not null," +
      "rawValues longtext not null," +
      "submitted tinyint not null," +
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
