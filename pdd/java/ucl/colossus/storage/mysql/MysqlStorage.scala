/*
 * Colossus is framework to build API servers, based on Finagle.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Accio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Accio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Colossus.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.colossus.storage.mysql

import com.google.common.util.concurrent.AbstractIdleService
import com.twitter.finagle.mysql._
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}
import ucl.colossus.api.{ApiResource, LabelWhere}
import ucl.colossus.codec.Codec
import ucl.colossus.storage._
import ucl.colossus.types.StructType

import scala.collection.mutable

private[storage] final class MysqlStorage(client: Client, codec: Codec)
  extends AbstractIdleService with Storage with Logging {

  override def get(key: String, structType: StructType): Future[Option[ApiResource]] = {
    client
      .prepare(s"select `value` from key_value where `key` = ?")
      .select(key)(decodeRow(_, structType))
      .map(_.headOption)
  }

  override def list(prefix: String, structType: StructType, predicate: SelectionPredicate): Future[ResultList] = {
    val where = mutable.ListBuffer.empty[String]
    val params = mutable.ListBuffer.empty[Parameter]

    where += s"`key` regexp ?"
    params += s"^$prefix[^/]+$$"

    predicate.fieldSelector.values.foreach { case (k, v) =>
      where += s"cast(json_unquote(json_extract(value, ?)) as char) = ?"
      params += s"$$.$k"
      params += v
    }

    predicate.labelSelector.conditions.foreach {
      case LabelWhere.Exists(key) =>
        where += s"json_contains_path(value, 'one', ?)"
        params += s"$$.metadata.labels.$key"
      case LabelWhere.Absent(key) =>
        where += s"not json_contains_path(value, 'one', ?)"
        params += s"$$.metadata.labels.$key"
      case LabelWhere.Equals(key, value) =>
        where += s"json_extract(value, ?) = ?"
        params += s"$$.metadata.labels.$key"
        params += value
      case LabelWhere.Different(key, value) =>
        where += s"(not json_contains_path(value, 'one', ?) or json_unquote(json_extract(value, ?)) <> ?)"
        params += s"$$.metadata.labels.$key"
        params += s"$$.metadata.labels.$key"
        params += value
      case LabelWhere.In(key, values) =>
        if (values.nonEmpty) {
          where += s"json_unquote(json_extract(value, ?)) in(${Seq.fill(values.size)("?").mkString(",")})"
          params += s"$$.metadata.labels.$key"
          values.foreach(v => params += v)
        }
      case LabelWhere.NotIn(key, values) =>
        if (values.nonEmpty) {
          where += s"(not json_contains_path(value, 'one', ?) or json_unquote(json_extract(value, ?)) not in(${Seq.fill(values.size)("?").mkString(",")}))"
          params += s"$$.metadata.labels.$key"
          params += s"$$.metadata.labels.$key"
          values.foreach(v => params += v)
        }
    }

    val sql = s"select `value` from key_value where ${where.mkString(" and ")}"
    client
      .prepare(sql)
      .select(params: _*)(decodeRow(_, structType))
      .map(ResultList.apply)
      .handle {
        case ServerError(code, _, message) =>
          logger.error(s"Error while retrieving '$prefix': $code/$message (SQL: $sql, params: ${params.map(_.value).mkString(", ")})")
          ResultList.empty
      }
  }

  override def create(key: String, obj: ApiResource): Future[Option[ApiResource]] = {
    MysqlMonitor.ignoring(1062) {
      val value = encodeValue(obj)
      client
        .prepare(s"insert into key_value(`key`, `value`) values(?, ?)")
        .apply(key, value)
        .map(_ => Some(obj))
        .rescue {
          // Error code 1062 corresponds to a duplicate entry, which means the object already exists.
          case ServerError(1062, _, _) => Future.value(None)
        }
    }
  }

  override def update(key: String, obj: ApiResource): Future[Option[ApiResource]] = {
    val value = encodeValue(obj)
    client
      .prepare("update key_value set `value` = ? where `key` = ?")
      .apply(value, key)
      .map {
        case ok: OK => if (ok.affectedRows == 1) Some(obj) else None
        case _ => None
      }
  }

  override def delete(key: String, structType: StructType): Future[Option[ApiResource]] = {
    get(key, structType).flatMap {
      case None => Future.value(None)
      case Some(obj) =>
        client
          .prepare(s"delete from key_value where `key` = ?")
          .apply(key)
          .map {
            case ok: OK => if (ok.affectedRows == 1) Some(obj) else None
            case _ => None
          }
    }
  }

  override protected def startUp(): Unit = {
    // The schema is automatically created if it does not exist.
    MysqlMonitor.ignoring(1146) {
      val f = client
        .query("select 1 from `key_value` limit 1")
        .rescue {
          // Error code 1146 corresponds to a table that does not exist.
          case ServerError(1146, _, _) =>
            logger.info("Creating MySQL storage schema")
            client.query(MysqlStorage.createSchemaScript)
            Future.Done
        }
      Await.result(f)
    }
  }

  override protected def shutDown(): Unit = client.close()

  private def encodeValue(obj: ApiResource): String = new String(encode(obj))

  private def decodeRow(row: Row, structType: StructType): ApiResource = {
    row("value")
      .map {
        case r: RawValue => decode(r.bytes, structType)
        case v => throw new RuntimeException(s"Invalid MySQL value type: ${v.getClass.getSimpleName}")
      }.get
    // `.get` should not fail as long as the above queries are well-formed. If there is no `value`
    // column, we should not arrive here and the query itself should have failed before.
  }

  private def decode(bytes: Array[Byte], structType: StructType): ApiResource =
    codec.serializer.decode(bytes, structType) match {
      case Left(e) => throw new RuntimeException(s"Error while decoding ${structType.kind.get}: $e")
      case Right(obj) => obj.asInstanceOf[ApiResource]
    }


  private def encode(obj: ApiResource): Array[Byte] = {
    codec.serializer.encode(obj) match {
      case Left(e) => throw new RuntimeException(s"Error while encoding ${obj.kind}: $e")
      case Right(bytes) => bytes
    }
  }
}

object MysqlStorage {
  private val createSchemaScript = {
    "CREATE TABLE `key_value`(" +
      "`unused_id` int not null auto_increment," +
      "`key` varchar(255) not null," +
      "`value` json not null," +
      "primary key (`unused_id`)," +
      "UNIQUE KEY `uix_key_value_name`(`key`)" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
  }
}
