package il.ac.afeka.cloud.projmvc.data

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

// Boundary for Manager assignment
data class ManagerEmailBoundary(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String? = null
)