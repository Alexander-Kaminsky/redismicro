package il.ac.afeka.cloud.projmvc.data

import il.ac.afeka.cloud.projmvc.validation.ValidDate // Import custom annotation
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import jakarta.persistence.Embeddable // Make it embeddable for JPA

@Embeddable // Can be embedded in the EmployeeEntity
@ValidDate // Apply custom validation at the class level
data class BirthdateBoundary(
    @field:NotBlank(message = "Day cannot be blank")
    @field:Pattern(regexp = "\\d{2}", message = "Day must be 2 digits")
    var day: String? = null, // Use var if JPA needs setters

    @field:NotBlank(message = "Month cannot be blank")
    @field:Pattern(regexp = "\\d{2}", message = "Month must be 2 digits")
    var month: String? = null,

    @field:NotBlank(message = "Year cannot be blank")
    @field:Pattern(regexp = "\\d{4}", message = "Year must be 4 digits")
    var year: String? = null
) {
    // Default constructor needed by JPA if using @Embeddable this way
    constructor() : this(null, null, null)

    fun toLocalDate(): LocalDate? {
        return try {
            // Basic check before parsing
            if (day == null || month == null || year == null) return null
            LocalDate.parse("$year-$month-$day", DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null // Validation annotation should catch structural issues
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