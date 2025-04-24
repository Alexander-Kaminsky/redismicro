package il.ac.afeka.cloud.redis.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed // Use for fields queried frequently
import java.time.LocalDate

@RedisHash("employee") // Maps to Redis keys like "employee:<email>"
data class RedisEmployeeEntity(
    @Id // Email is the Redis key
    val email: String,

    var name: String,

    // WARNING: Storing plain password - Highly insecure! Hash in production.
    var password: String,

    // Store date as ISO string (YYYY-MM-DD) for better compatibility & readability in Redis
    // Alternatively, store epoch day/milliseconds, but string is often simpler.
    var birthdate: String, // Store as String (ISO YYYY-MM-DD)

    @Indexed // Indexing roles can help if using Redis search capabilities or specific queries
    var roles: Set<String>, // Set for uniqueness

    // --- Bonus Fields ---
    @Indexed // Index manager email to efficiently find subordinates
    var managerEmail: String? = null // Email of the manager
    // No explicit subordinates list; query by managerEmail
) {
    // Helper to convert stored string back to LocalDate
    fun getBirthdateAsLocalDate(): LocalDate {
        return LocalDate.parse(birthdate)
    }
}