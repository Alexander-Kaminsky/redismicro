package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.DateTimeException
import java.time.LocalDate

class ValidDateValidator : ConstraintValidator<ValidDate, DateInfo> {
    override fun isValid(value: DateInfo, context: ConstraintValidatorContext): Boolean {
        return try {
            LocalDate.of(value.year, value.month, value.day)
            true
        } catch (e: DateTimeException) {
            false
        }
    }
}
