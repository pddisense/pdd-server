package ucl.pdd.api

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

case class ErrorCause(field: Option[String], message: String)

object ErrorCause {
  def apply(message: String, field: String): ErrorCause = ErrorCause(Some(field), message)

  def apply(message: String): ErrorCause = ErrorCause(None, message)
}
