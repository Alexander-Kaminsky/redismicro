package il.ac.afeka.cloud.redis.data

import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class EmployeeBoundary(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String? = null,

    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 3, message = "Password must be at least 3 characters long")
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
        message = "Password must contain at least one uppercase letter and one digit"
    )
    val password: String? = null,

    @field:NotNull(message = "Birthdate cannot be null")
    @field:Valid // Enable validation for nested BirthdateBoundary
    val birthdate: BirthdateBoundary? = null,

    @field:NotEmpty(message = "Roles cannot be empty")
    val roles: List<@NotBlank(message = "Role cannot be blank") String>? = null
)