package ucl.pdd.storage.mysql

import java.sql.Timestamp
import java.util.TimeZone

import com.twitter.finagle.mysql.transport.MysqlBufWriter
import com.twitter.finagle.mysql.{CanBeParameter, Parameter, TimestampValue}
import org.joda.time.Instant

sealed trait Where {
  def params: Seq[Parameter]

  def sql: String
}

object Where {
  private val timestampValue = new TimestampValue(TimeZone.getDefault, TimeZone.getTimeZone("UTC"))

  case class Equals(field: String, value: Parameter) extends Where {
    override def sql: String = s"$field = ?"

    override def params: Seq[Parameter] = Seq(value)
  }

  case class In(field: String, values: Parameter*) extends Where {
    override def sql: String = s"$field = ?"

    override def params: Seq[Parameter] = values
  }

  case class NotEquals(field: String, value: Parameter) extends Where {
    override def sql: String = s"$field <> ?"

    override def params: Seq[Parameter] = Seq(value)
  }

  case class GreaterThan(field: String, value: Parameter) extends Where {
    override def sql: String = s"$field > ?"

    override def params: Seq[Parameter] = Seq(value)
  }

  case class AtLeast(field: String, value: Parameter) extends Where {
    override def sql: String = s"$field >= ?"

    override def params: Seq[Parameter] = Seq(value)
  }

  case class LessThan(field: String, value: Parameter) extends Where {
    override def sql: String = s"$field < ?"

    override def params: Seq[Parameter] = Seq(value)
  }

  case class AtMost(field: String, value: Parameter) extends Where {
    override def sql: String = s"$field <= ?"

    override def params: Seq[Parameter] = Seq(value)
  }

  private abstract class CanBeParameterWrapper[A, B] extends CanBeParameter[A] {
    override def sizeOf(param: A): Int = underlying.sizeOf(encode(param))

    override def typeCode(param: A): Short = underlying.typeCode(encode(param))

    override def write(writer: MysqlBufWriter, param: A): Unit = underlying.write(writer, encode(param))

    protected def encode(v: A): B

    protected def underlying: CanBeParameter[B]
  }

  implicit def wrapInstant(instant: Instant): Parameter = Parameter.wrap(timestampValue(new Timestamp(instant.getMillis)))

  implicit def wrapInstant(instant: Option[Instant]): Parameter = instant.map(wrapInstant).getOrElse(Parameter.NullParameter)

  implicit def wrapString(str: String): Parameter = Parameter.wrap(str)

  implicit def wrapString(str: Option[String]): Parameter = Parameter.wrap(str.getOrElse(""))

  implicit def wrapStrings(strs: Seq[String]): Parameter = Parameter.wrap(strs.mkString("\n"))

  implicit def wrapStrings(strs: Option[Seq[String]]): Parameter = strs.map(wrapStrings).getOrElse("")

  implicit def wrapDouble(dbl: Option[Double]): Parameter = dbl.map(Parameter.wrap(_)).getOrElse(Parameter.NullParameter)

  implicit def wrapDoubles(dbls: Seq[Double]): Parameter = Parameter.wrap(dbls.mkString("\n"))

  implicit def wrapDoubles(dbls: Option[Seq[Double]]): Parameter = dbls.map(wrapDoubles).getOrElse("")

  implicit def wrapLongs(lngs: Seq[Long]): Parameter = Parameter.wrap(lngs.mkString("\n"))

  implicit def wrapLongs(lngs: Option[Seq[Long]]): Parameter = lngs.map(wrapLongs).getOrElse("")
}
