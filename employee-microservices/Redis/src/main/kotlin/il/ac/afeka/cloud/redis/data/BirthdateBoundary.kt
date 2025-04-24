package il.ac.afeka.cloud.redis.data

import il.ac.afeka.cloud.redis.customvalidation.ValidDate // Adjust import if validation is in different package
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ValidDate // Apply custom validation at the class level
data class BirthdateBoundary(
    @field:NotBlank(message = "Day cannot be blank")
    @field:Pattern(regexp = "\\d{2}", message = "Day must be 2 digits")
    val day: String? = null,

    @field:NotBlank(message = "Month cannot be blank")
    @field:Pattern(regexp = "\\d{2}", message = "Month must be 2 digits")
    val month: String? = null,

    @field:NotBlank(message = "Year cannot be blank")
    @field:Pattern(regexp = "\\d{4}", message = "Year must be 4 digits")
    val year: String? = null
) {
    fun toLocalDate(): LocalDate? {
        return try {
            if (day == null || month == null || year == null) return null
            LocalDate.parse("$year-$month-$day", DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun fromLocalDate(date: LocalDate): BirthdateBoundary {
            return BirthdateBoundary(
                day = date.format(DateTimeFormatter.ofPattern("dd")),
                month = date.format(DateTimeFormatter.ofPattern("MM")),
                year = date.format(DateTimeFormatter.ofPattern("yyyy"))
            )
        }
    }
}