package ucl.pdd.api

import scala.collection.mutable

object ClientValidator {
  def validate(obj: Client): ValidationResult = {
    val errors = mutable.ListBuffer.empty[ErrorCause]
    if (obj.publicKey.isEmpty) {
      errors += ErrorCause("Should not be empty", "publicKey")
    }
    if (obj.browser.isEmpty) {
      errors += ErrorCause("Should not be empty", "browser")
    }

    if (errors.isEmpty) ValidationResult.Valid else ValidationResult.Invalid(errors.toList)
  }
}
