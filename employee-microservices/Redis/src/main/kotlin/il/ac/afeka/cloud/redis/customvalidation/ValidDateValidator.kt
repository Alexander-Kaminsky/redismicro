package il.ac.afeka.cloud.redis.customvalidation

import il.ac.afeka.cloud.redis.data.BirthdateBoundary // Adjust import
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class ValidDateValidator : ConstraintValidator<ValidDate, BirthdateBoundary> {

    override fun isValid(value: BirthdateBoundary?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }
        val day = value.day
        val month = value.month
        val year = value.year
        if (day == null || month == null || year == null) {
            return false
        }
        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT)
            LocalDate.parse("$year-$month-$day", formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}