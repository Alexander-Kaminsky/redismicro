package il.ac.afeka.cloud.projmvc.validation

import il.ac.afeka.cloud.projmvc.data.BirthdateBoundary
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle // For strict date validation

class ValidDateValidator : ConstraintValidator<ValidDate, BirthdateBoundary> {

    override fun isValid(value: BirthdateBoundary?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true // Let @NotNull handle null checks on the field itself
        }

        // Individual fields are already checked for pattern/blank by other annotations.
        // Here, we check if they form a *valid* date together.
        val day = value.day
        val month = value.month
        val year = value.year

        if (day == null || month == null || year == null) {
            // This case might occur if @NotBlank failed but validation continues.
            // Or if called programmatically with nulls. Let other constraints handle it.
            return false // Or true depending on desired behavior when components are missing
        }


        return try {
            // Use a strict resolver to prevent dates like Feb 30th
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT)
            LocalDate.parse("$year-$month-$day", formatter)
            true // Successfully parsed a valid date
        } catch (e: DateTimeParseException) {
            // Add specific violation message if needed
            // context?.disableDefaultConstraintViolation()
            // context?.buildConstraintViolationWithTemplate("Date '$year-$month-$day' is invalid.")?.addConstraintViolation()
            false // Parsing failed, date is invalid
        } catch (e: Exception) {
            // Catch other potential errors during parsing
            false
        }
    }
}