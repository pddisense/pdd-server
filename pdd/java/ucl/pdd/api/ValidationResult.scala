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
  override def toString: String = field.map(_ + ": ").getOrElse("") + message
}

object ErrorCause {
  def apply(message: String, field: String): ErrorCause = ErrorCause(Some(field), message)

  def apply(message: String): ErrorCause = ErrorCause(None, message)
}
