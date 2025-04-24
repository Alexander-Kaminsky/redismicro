package il.ac.afeka.cloud.redis.data

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ManagerEmailBoundary(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String? = null
)