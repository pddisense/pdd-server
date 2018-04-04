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

private[mysql] final class MysqlCampaignStore(mysql: MysqlClient)
  extends CampaignStore with MysqlStore {

  private[this] val query = new QueryBuilder(mysql, "campaigns", hydrate)

  import MysqlStore._

  override def list(q: CampaignStore.Query): Future[Seq[Campaign]] = {
    val qb = query.select.orderBy("createTime", "desc")
    q.isActive.foreach { isActive =>
      qb.where("startTime is not null")
        .where("startTime <= now()")
        .where("endTime is null or endTime > now()")
      if (!isActive) {
        qb.not()
      }
    }
    qb.execute()
  }

  override def get(name: String): Future[Option[Campaign]] = {
    val qb = query.select.where("name = ?", name).limit(1)
    qb.execute().map(_.headOption)
  }

  override def batchGet(names: Seq[String]): Future[Seq[Option[Campaign]]] = {
    if (names.isEmpty) {
      Future.value(Seq.empty)
    } else {
      query
        .select
        .where(s"name in (${Seq.fill(names.size)("?").mkString(",")})", names.map(wrapString): _*)
        .execute()
        .map(campaigns => names.map(name => campaigns.find(_.name == name)))
    }
  }

  override def create(campaign: Campaign): Future[Boolean] = {
    query
      .insert
      .set("name", campaign.name)
      .set("createTime", campaign.createTime)
      .set("displayName", campaign.displayName)
      .set("email", encodeEmail(campaign.email))
      .set("vocabulary", encodeVocabulary(campaign.vocabulary))
      .set("startTime", campaign.startTime)
      .set("endTime", campaign.endTime)
      .set("collectRaw", campaign.collectRaw)
      .set("collectEncrypted", campaign.collectEncrypted)
      .set("delay", campaign.delay)
      .set("graceDelay", campaign.graceDelay)
      .set("groupSize", campaign.groupSize)
      .set("samplingRate", campaign.samplingRate)
      .execute()
      .map(_ => true)
      .rescue {
        // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
        case ServerError(1062, _, _) => Future.value(false)
      }
  }

  override def replace(campaign: Campaign): Future[Boolean] = {
    query
      .update
      .where("name = ?", campaign.name)
      .set("createTime", campaign.createTime)
      .set("displayName", campaign.displayName)
      .set("email", encodeEmail(campaign.email))
      .set("vocabulary", encodeVocabulary(campaign.vocabulary))
      .set("startTime", campaign.startTime)
      .set("endTime", campaign.endTime)
      .set("collectRaw", campaign.collectRaw)
      .set("collectEncrypted", campaign.collectEncrypted)
      .set("delay", campaign.delay)
      .set("graceDelay", campaign.graceDelay)
      .set("groupSize", campaign.groupSize)
      .set("samplingRate", campaign.samplingRate)
      .execute()
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
