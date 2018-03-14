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

import java.sql.Timestamp
import java.util.TimeZone

import com.twitter.finagle.mysql._
import org.joda.time.Instant

private[mysql] trait MysqlStore {

  import MysqlStore._

  protected final def getString(row: Row, field: String): Option[String] =
    row(field).flatMap {
      case StringValue("") => None
      case StringValue(s) => Some(s)
      case NullValue => None
    }

  protected final def getInt(row: Row, field: String): Option[Int] =
    row(field).flatMap {
      case IntValue(i) => Some(i)
      case NullValue => None
    }

  protected final def getInstant(row: Row, field: String): Option[Instant] =
    row(field).flatMap {
      case timestampValue(timestamp) => Some(new Instant(timestamp.getTime))
      case NullValue => None
    }
}

object MysqlStore {
  private val timestampValue = new TimestampValue(TimeZone.getDefault, TimeZone.getTimeZone("UTC"))

  implicit def wrapInstant(instant: Instant): Parameter = Parameter.wrap(timestampValue(new Timestamp(instant.getMillis)))

  implicit def wrapInstant(instant: Option[Instant]): Parameter = instant.map(wrapInstant).getOrElse(Parameter.NullParameter)

  implicit def wrapString(str: Option[String]): Parameter = Parameter.wrap(str.getOrElse(""))
}
