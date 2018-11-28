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

package ucl.pdd.storage.mysql.query

import com.twitter.finagle.mysql.{Client, Parameter, Result, Row}
import com.twitter.util.Future

import scala.collection.mutable

private[mysql] class QueryBuilder[T](mysql: Client, table: String, hydrator: Row => T) {
  def select: SelectQuery = new SelectQuery(table)

  def update: UpdateQuery = new UpdateQuery(table)

  def insert: InsertQuery = new InsertQuery(table)

  def delete: DeleteQuery = new DeleteQuery(table)

  trait Query {
    protected def build: (String, Seq[Parameter])
  }

  trait AlterMixin {
    this: Query =>

    def execute(): Future[Result] = {
      val (sql, params) = build
      mysql.prepare(sql).apply(params: _*)
    }
  }

  trait WhereMixin {
    this: Query =>

    private[this] val _where = mutable.ListBuffer.empty[(String, Seq[Parameter])]
    private[this] var _not = false

    final def where(clause: String, params: Parameter*): this.type = {
      _where += (clause -> params)
      this
    }

    final def whereIn(field: String, values: Parameter*): this.type = {
      if (values.isEmpty) {
        where("false")
      } else {
        where(s"name in (${Seq.fill(values.size)("?").mkString(",")})", values: _*)
      }
    }

    final def not(): this.type = {
      _not = true
      this
    }

    protected final def buildWhere(sb: mutable.StringBuilder, params: mutable.ListBuffer[Parameter]): Unit = {
      if (_where.nonEmpty) {
        sb ++= " where"
        if (_not) {
          sb ++= " not"
        }
        sb ++= " (( "
        sb ++= _where.map(_._1).mkString(") and (")
        sb ++= ")) "
      }
      params ++= _where.flatMap(_._2)
    }
  }

  final class InsertQuery(table: String) extends Query with AlterMixin {
    private[this] val _values = mutable.ListBuffer.empty[(String, Parameter)]

    def set(field: String, value: Parameter): this.type = {
      _values += (field -> value)
      this
    }

    override protected def build: (String, Seq[Parameter]) = {
      val sb = new mutable.StringBuilder()
      sb ++= "insert into "
      sb ++= table
      sb ++= " ("
      sb ++= _values.map(_._1).mkString(", ")
      sb ++= ") values ("
      sb ++= Seq.fill(_values.size)("?").mkString(", ")
      sb += ')'
      (sb.result(), _values.map(_._2))
    }
  }

  final class UpdateQuery(table: String) extends Query with WhereMixin with AlterMixin {
    private[this] val _values = mutable.ListBuffer.empty[(String, Parameter)]

    def set(field: String, value: Parameter): this.type = {
      _values += (field -> value)
      this
    }

    override protected def build: (String, Seq[Parameter]) = {
      val sb = new mutable.StringBuilder()
      val params = mutable.ListBuffer.empty[Parameter]
      sb ++= "update "
      sb ++= table
      sb ++= " set "
      sb ++= _values.map { case (k, v) => s"$k = ?" }.mkString(", ")
      params ++= _values.map(_._2)
      buildWhere(sb, params)
      (sb.result(), params)
    }
  }

  final class DeleteQuery(table: String) extends Query with WhereMixin with AlterMixin {
    override protected def build: (String, Seq[Parameter]) = {
      val sb = new mutable.StringBuilder()
      val params = mutable.ListBuffer.empty[Parameter]
      sb ++= "delete from "
      sb ++= table
      buildWhere(sb, params)
      (sb.result(), params)
    }
  }

  final class SelectQuery(table: String) extends Query with WhereMixin {
    private[this] val _select = mutable.ListBuffer.empty[String]
    private[this] var _orderBy: Option[String] = None
    private[this] var _limit: Option[Int] = None

    def select(fields: String*): this.type = {
      _select ++= fields
      this
    }

    def orderBy(field: String, ord: String): this.type = {
      _orderBy = Some(field + " " + ord)
      this
    }

    def limit(n: Int): this.type = {
      _limit = Some(n)
      this
    }

    def count(): Future[Int] = {
      _select.prepend("count(1) as _count")
      val (sql, params) = build
      mysql.prepare(sql).select(params: _*)(row => row.longOrZero("_count")).map(_.head.toInt)
    }

    def execute(): Future[Seq[T]] = {
      val (sql, params) = build
      mysql.prepare(sql).select(params: _*)(hydrator)
    }

    override protected def build: (String, Seq[Parameter]) = {
      val sb = new mutable.StringBuilder()
      val params = mutable.ListBuffer.empty[Parameter]
      sb ++= "select "
      if (_select.isEmpty) {
        sb += '*'
      } else {
        sb ++= _select.mkString(", ")
      }
      sb ++= " from "
      sb ++= table
      buildWhere(sb, params)
      _orderBy.foreach { orderBy =>
        sb ++= " order by "
        sb ++= orderBy
      }
      _limit.foreach { limit =>
        sb ++= " limit "
        sb ++= limit.toString
      }
      (sb.result(), params)
    }
  }

}
