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

import com.twitter.finagle.mysql.Parameter

import scala.collection.mutable

private[mysql] abstract class QueryBuilder(from: String) {
  private[this] val _where = mutable.ListBuffer.empty[String]
  private[this] var _not = false
  private[this] val _params = mutable.ListBuffer.empty[Parameter]

  def sql: String

  final def params: Seq[Parameter] = _params

  final def where(clause: String, params: Parameter*): this.type = {
    _where += clause
    _params ++= params
    this
  }

  final def not(): this.type = {
    _not = true
    this
  }

  protected final def addWhere(sb: mutable.StringBuilder): Unit = {
    if (_where.nonEmpty) {
      sb ++= " where"
      if (_not) {
        sb ++= " not"
      }
      sb ++= " (( "
      sb ++= _where.mkString(") and (")
      sb ++= ")) "
    }
  }
}

object QueryBuilder {

  def select(from: String): Select = new Select(from)

  final class Select(from: String) extends QueryBuilder(from) {
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

    override def sql: String = {
      val sb = new mutable.StringBuilder()
      sb ++= "select "
      if (_select.isEmpty) {
        sb += '*'
      } else {
        sb ++= _select.mkString(", ")
      }
      sb ++= " from "
      sb ++= from
      addWhere(sb)
      _orderBy.foreach { orderBy =>
        sb ++= " order by "
        sb ++= orderBy
      }
      _limit.foreach { limit =>
        sb ++= " limit "
        sb ++= limit.toString
      }
      sb.result()
    }
  }

}
