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

import com.twitter.finagle.mysql.{OK, Row, ServerError, Client => MysqlClient}
import com.twitter.util.Future
import ucl.pdd.domain.{Campaign, Vocabulary}
import ucl.pdd.storage.CampaignStore
import ucl.pdd.storage.mysql.query.QueryBuilder

private[mysql] final class MysqlCampaignStore(mysql: MysqlClient)
  extends CampaignStore with MysqlStore {

  private[this] val query = new QueryBuilder(mysql, "campaigns", hydrate)

  import MysqlStore._

  override def list(q: CampaignStore.Query): Future[Seq[Campaign]] = {
    val qb = query.select.orderBy("createTime", "desc")
    addWhere(qb, q)
    qb.execute()
  }

  override def count(q: CampaignStore.Query): Future[Int] = {
    val qb = query.select
    addWhere(qb, q)
    qb.count()
  }

  override def get(name: String): Future[Option[Campaign]] = {
    query.select
      .where("name = ?", name).limit(1)
      .execute()
      .map(_.headOption)
  }

  override def delete(name: String): Future[Unit] = {
    query.delete.where("name = ?", name).execute().unit
  }

  override def multiGet(names: Seq[String]): Future[Seq[Option[Campaign]]] = {
    if (names.isEmpty) {
      Future.value(Seq.empty)
    } else {
      query.select
        .whereIn("name", names.map(wrapString): _*)
        .execute()
        .map(campaigns => names.map(name => campaigns.find(_.name == name)))
    }
  }

  override def create(campaign: Campaign): Future[Boolean] = {
    query.insert
      .set("name", campaign.name)
      .set("createTime", campaign.createTime)
      .set("displayName", campaign.displayName)
      .set("email", campaign.email)
      .set("notes", campaign.notes)
      .set("vocabulary", encodeVocabulary(campaign.vocabulary))
      .set("startTime", campaign.startTime)
      .set("endTime", campaign.endTime)
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
    query.update
      .where("name = ?", campaign.name)
      .set("createTime", campaign.createTime)
      .set("displayName", campaign.displayName)
      .set("email", campaign.email)
      .set("notes", campaign.notes)
      .set("vocabulary", encodeVocabulary(campaign.vocabulary))
      .set("startTime", campaign.startTime)
      .set("endTime", campaign.endTime)
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
    val vocabulary = Vocabulary(toString(row, "vocabulary")
      .split("\n")
      .filter(_.nonEmpty)
      .map(decodeQuery))
    Campaign(
      name = toString(row, "name"),
      createTime = toInstant(row, "createTime"),
      displayName = toString(row, "displayName"),
      email = getString(row, "email"),
      notes = getString(row, "notes"),
      vocabulary = vocabulary,
      startTime = getInstant(row, "startTime"),
      endTime = getInstant(row, "endTime"),
      delay = toInt(row, "delay"),
      graceDelay = toInt(row, "graceDelay"),
      groupSize = toInt(row, "groupSize"),
      samplingRate = getDouble(row, "samplingRate"))
  }

  private def decodeQuery(str: String): Vocabulary.Query =
    if (str.contains(",")) {
      Vocabulary.Query(terms = Some(str.split(",").filter(_.nonEmpty)))
    } else {
      Vocabulary.Query(exact = Some(str))
    }

  private def encodeVocabulary(vocabulary: Vocabulary) = {
    vocabulary.queries.map(encodeQuery).mkString("\n")
  }

  private def encodeQuery(query: Vocabulary.Query) = {
    if (query.terms.isDefined) {
      query.terms.get.mkString(",") + ","
    } else {
      query.exact.getOrElse("")
    }
  }

  private def addWhere(qb: query.SelectQuery, q: CampaignStore.Query): Unit = {
    q.isActive.foreach { isActive =>
      qb.where("startTime is not null")
        .where("startTime <= now()")
        .where("endTime is null or endTime > now()")
      if (!isActive) {
        qb.not()
      }
    }
  }
}
