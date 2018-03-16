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

import com.twitter.finagle.mysql.{OK, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.api.{Campaign, Vocabulary, VocabularyQuery}
import ucl.pdd.storage.CampaignStore

import scala.collection.mutable

private[mysql] final class MysqlCampaignStore(mysql: MysqlClient) extends CampaignStore with MysqlStore {

  import MysqlStore._

  override def list(query: CampaignStore.Query): Future[Seq[Campaign]] = {
    val where = mutable.ListBuffer.empty[String]
    query.isActive.foreach {
      case true => where += "startTime is not null and startTime <= now() and (endTime is null or endTime > now())"
      case false => where += "startTime is null or startTime > now() or (endTime is not null and endTime <= now())"
    }
    mysql
      .prepare("select * " +
        "from campaigns " +
        s"where ${if (where.isEmpty) "true" else where.mkString(" and ")} " +
        "order by createTime desc")
      .select()(hydrate)
  }

  override def get(name: String): Future[Option[Campaign]] = {
    mysql
      .prepare("select * from campaigns where name = ? limit 1")
      .select(name)(hydrate)
      .map(_.headOption)
  }

  override def create(campaign: Campaign): Future[Boolean] = {
    val sql = "insert into campaigns(name, createTime, displayName, email, vocabulary, startTime, " +
      "endTime, collectRaw, collectEncrypted, delay, graceDelay, groupSize, samplingRate) " +
      "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    mysql
      .prepare(sql)
      .apply(
        campaign.name,
        campaign.createTime,
        campaign.displayName,
        encodeEmail(campaign.email),
        encodeVocabulary(campaign.vocabulary),
        campaign.startTime,
        campaign.endTime,
        campaign.collectRaw,
        campaign.collectEncrypted,
        campaign.delay,
        campaign.graceDelay,
        campaign.groupSize,
        campaign.samplingRate)
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(campaign: Campaign): Future[Boolean] = {
    val sql = "update campaigns " +
      "set createTime = ?, displayName = ?, email = ?, vocabulary = ?, startTime = ?, endTime = ?, " +
      "collectRaw = ?, collectEncrypted = ?, delay = ?, graceDelay = ?, groupSize = ?, samplingRate = ? " +
      "where name = ?"
    mysql
      .prepare(sql)
      .apply(
        campaign.createTime,
        campaign.displayName,
        encodeEmail(campaign.email),
        encodeVocabulary(campaign.vocabulary),
        campaign.startTime,
        campaign.endTime,
        campaign.collectRaw,
        campaign.collectEncrypted,
        campaign.delay,
        campaign.graceDelay,
        campaign.groupSize,
        campaign.samplingRate,
        campaign.name)
      .map {
        case ok: OK => ok.affectedRows == 1
        case _ => false
      }
  }

  private def hydrate(row: Row): Campaign = {
    val email = toString(row, "email").split("\n").filter(_.nonEmpty)
    val vocabulary = Vocabulary(toString(row, "vocabulary")
      .split("\n")
      .filter(_.nonEmpty)
      .map(decodeQuery))
    Campaign(
      name = toString(row, "name"),
      createTime = toInstant(row, "createTime"),
      displayName = toString(row, "displayName"),
      email = email,
      vocabulary = vocabulary,
      startTime = getInstant(row, "startTime"),
      endTime = getInstant(row, "endTime"),
      collectRaw = toBoolean(row, "collectRaw"),
      collectEncrypted = toBoolean(row, "collectEncrypted"),
      delay = toInt(row, "delay"),
      graceDelay = toInt(row, "graceDelay"),
      groupSize = toInt(row, "groupSize"),
      samplingRate = getDouble(row, "samplingRate"))
  }

  private def decodeQuery(str: String): VocabularyQuery =
    if (str.contains(",")) {
      VocabularyQuery(terms = Some(str.split(",").filter(_.nonEmpty)))
    } else {
      VocabularyQuery(exact = Some(str))
    }

  private def encodeEmail(email: Seq[String]) = email.mkString("\n")

  private def encodeVocabulary(vocabulary: Vocabulary) = {
    vocabulary.queries.map(encodeQuery).mkString("\n")
  }

  private def encodeQuery(query: VocabularyQuery) = {
    if (query.terms.isDefined) {
      query.terms.get.mkString(",") + ","
    } else {
      query.exact.getOrElse("")
    }
  }
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
