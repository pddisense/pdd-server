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
