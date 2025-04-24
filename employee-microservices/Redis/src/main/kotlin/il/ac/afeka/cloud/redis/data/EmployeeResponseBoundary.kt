package il.ac.afeka.cloud.redis.data

data class EmployeeResponseBoundary(
    val email: String,
    val name: String,
    val birthdate: BirthdateBoundary,
    val roles: List<String>
)