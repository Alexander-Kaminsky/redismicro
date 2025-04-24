package il.ac.afeka.cloud.projmvc.data

// Output Boundary for GET requests (excludes password)
data class EmployeeResponseBoundary(
    val email: String,
    val name: String,
    val birthdate: BirthdateBoundary,
    val roles: List<String>
)