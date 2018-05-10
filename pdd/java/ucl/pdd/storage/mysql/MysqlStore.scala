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

import java.sql.Timestamp
import java.util.TimeZone

import com.twitter.finagle.mysql._
import org.joda.time.Instant

private[mysql] trait MysqlStore {

  import MysqlStore._

  protected final def toString(row: Row, field: String): String = getString(row, field).getOrElse("")

  protected final def getString(row: Row, field: String): Option[String] =
    row(field).flatMap {
      case StringValue("") => None
      case StringValue(s) => Some(s)
      case NullValue => None
      case v => throw new IllegalArgumentException(s"Not a string: $v")
    }

  protected final def toInt(row: Row, field: String): Int = getInt(row, field).getOrElse(0)

  protected final def getInt(row: Row, field: String): Option[Int] =
    row(field).flatMap {
      case IntValue(i) => Some(i)
      case NullValue => None
      case v => throw new IllegalArgumentException(s"Not an int: $v")
    }

  protected final def toLong(row: Row, field: String): Long = getLong(row, field).getOrElse(0)

  protected final def getLong(row: Row, field: String): Option[Long] =
    row(field).flatMap {
      case IntValue(i) => Some(i.toLong)
      case LongValue(l) => Some(l)
      case NullValue => None
      case v => throw new IllegalArgumentException(s"Not a long: $v")
    }

  protected final def toLongs(row: Row, field: String): Seq[Long] = {
    getLongs(row, field).getOrElse(Seq.empty)
  }

  protected final def getLongs(row: Row, field: String): Option[Seq[Long]] =
    getString(row, field).map { str =>
      str.split(' ').filter(_.nonEmpty).map(_.toLong).toSeq
    }

  protected final def toStrings(row: Row, field: String): Seq[String] = {
    getStrings(row, field).getOrElse(Seq.empty)
  }

  protected final def getStrings(row: Row, field: String): Option[Seq[String]] =
    getString(row, field).map { str =>
      str.split("\n").filter(_.nonEmpty).toSeq
    }

  protected final def toDouble(row: Row, field: String): Double = getDouble(row, field).getOrElse(0)

  protected final def getDouble(row: Row, field: String): Option[Double] =
    row(field).flatMap {
      case DoubleValue(d) => Some(d)
      case NullValue => None
      case v => throw new IllegalArgumentException(s"Not a double: $v")
    }

  protected final def toInstant(row: Row, field: String): Instant = getInstant(row, field).getOrElse(new Instant(0))

  protected final def getInstant(row: Row, field: String): Option[Instant] =
    row(field).flatMap {
      case timestampValue(timestamp) => Some(new Instant(timestamp.getTime))
      case NullValue => None
      case v => throw new IllegalArgumentException(s"Not a timestamp: $v")
    }

  protected final def toBoolean(row: Row, field: String): Boolean = getBoolean(row, field).getOrElse(false)

  protected final def getBoolean(row: Row, field: String): Option[Boolean] =
    row(field).flatMap {
      case IntValue(i) => Some(i != 0)
      case ByteValue(b) => Some(b != 0)
      case NullValue => None
      case v => throw new IllegalArgumentException(s"Not a boolean: $v")
    }
}

object MysqlStore {
  private val timestampValue = new TimestampValue(TimeZone.getDefault, TimeZone.getTimeZone("UTC"))

  implicit def wrapInstant(instant: Instant): Parameter = Parameter.wrap(timestampValue(new Timestamp(instant.getMillis)))

  implicit def wrapInstant(instant: Option[Instant]): Parameter = instant.map(wrapInstant).getOrElse(Parameter.NullParameter)

  implicit def wrapString(str: String): Parameter = Parameter.wrap(str)

  implicit def wrapString(str: Option[String]): Parameter = Parameter.wrap(str.getOrElse(""))

  implicit def wrapStrings(strs: Seq[String]): Parameter = Parameter.wrap(strs.mkString("\n"))

  implicit def wrapStrings(strs: Option[Seq[String]]): Parameter = strs.map(wrapStrings).getOrElse("")

  implicit def wrapDouble(dbl: Option[Double]): Parameter = dbl.map(Parameter.wrap(_)).getOrElse(Parameter.NullParameter)

  implicit def wrapLongs(lngs: Seq[Long]): Parameter = Parameter.wrap(lngs.mkString(" "))

  implicit def wrapLongs(lngs: Option[Seq[Long]]): Parameter = lngs.map(wrapLongs).getOrElse("")
}
