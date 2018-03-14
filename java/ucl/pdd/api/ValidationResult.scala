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

package ucl.pdd.api

import com.fasterxml.jackson.annotation.JsonValue

sealed trait ValidationResult {
  def ++(other: ValidationResult): ValidationResult
}

object ValidationResult {

  case object Valid extends ValidationResult {
    override def ++(other: ValidationResult): ValidationResult =
      other match {
        case Valid => Valid
        case o: Invalid => o
      }
  }

  case class Invalid(errors: Seq[ErrorCause]) extends ValidationResult {
    override def ++(other: ValidationResult): ValidationResult =
      other match {
        case Valid => this
        case o: Invalid => Invalid(errors ++ o.errors)
      }
  }

}

case class ErrorCause(field: Option[String], message: String) {
  @JsonValue
  override def toString: String = field.map(_ + ": ") + message
}

object ErrorCause {
  def apply(message: String, field: String): ErrorCause = ErrorCause(Some(field), message)

  def apply(message: String): ErrorCause = ErrorCause(None, message)
}
