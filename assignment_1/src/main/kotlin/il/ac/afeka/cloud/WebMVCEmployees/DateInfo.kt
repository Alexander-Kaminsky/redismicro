package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.io.Serializable
import java.time.LocalDateTime

data class DateInfo(
    @field:Min(1, message = "day must be at least 1")
    @field:Max(31, message = "day must be at most 31")
    @JsonSerialize(using = TwoDigitSerializer::class)
    val day: Int,

    @field:Min(1, message = "month must be at least 1")
    @field:Max(12, message = "month must be at most 12")
    @JsonSerialize(using = TwoDigitSerializer::class)
    val month: Int,

    @field:Min(1, message = "year must be at least 1. you did not born before jesus")
    @field:Max(2025, message = "year must be at most 2025, how did you born in the future?")
    @JsonSerialize(using = FourDigitSerializer::class)
    val year: Int
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

fun LocalDateTime.toDateInfo(): DateInfo {
    return DateInfo(
        day = this.dayOfMonth,
        month = this.monthValue,
        year = this.year
    )
}


fun DateInfo.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(this.year, this.month, this.day, 0, 0)
}

fun DateInfo.isBefore(other: LocalDateTime): Boolean {
    return this.toLocalDateTime().isBefore(other)
}

fun DateInfo.isAfter(other: LocalDateTime): Boolean {
    return this.toLocalDateTime().isAfter(other)
}
