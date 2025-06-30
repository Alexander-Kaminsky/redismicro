package redisImp

import il.ac.afeka.cloud.WebMVCEmployees.EmployeeNotFoundException
import il.ac.afeka.cloud.WebMVCEmployees.InvalidInputException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
data class RedisEmployeeService(
    private val repo: RedisEmployeeRepository,
    private val redisTemplate: RedisTemplate<String, String> // Inject RedisTemplate for direct Redis access
) : EmployeeService {

    override fun createEmployee(employee: EmployeeBoundary): EmployeeBoundary {
        if (repo.existsById(employee.email!!))
            throw InvalidInputException("Email already exists")

        validateEmployeeDetails(employee)

        val employeeHashKey = "employee:${employee.email}"
        val birthDateTime = EmployeeBoundary.dateInfoToDate(employee.birthDate)
            ?: throw InvalidInputException("Invalid birth date")

        val employeeData = mapOf(
            "name" to employee.name,
            "password" to employee.password,
            "birthTimestamp" to birthDateTime.toString(),
            "roles" to employee.roles?.joinToString(",")
        )

        // Save to Redis Hash
        redisTemplate.opsForHash<String, String>().putAll(employeeHashKey, employeeData)

        // Save to Sorted Set by Age (for efficient age-based queries)
        val birthDateAsScore = birthDateTime.toEpochSecond(ZoneOffset.UTC)
        redisTemplate.opsForZSet().add("employeeByAge", employee.email, birthDateAsScore.toDouble())

        // Index by domain for efficient domain-based queries
        employee.roles?.forEach { role ->
            redisTemplate.opsForSet().add("role:$role", employee.email)
        }

        // Index by domain for email domain-based queries
        val normalizedDomain = employee.email.split("@")[1]
        redisTemplate.opsForSet().add("domain:$normalizedDomain", employee.email)

        // Save minimal record to repo if needed for pagination
        repo.save(employee.toRedisEntity())

        return EmployeeBoundary(
            email = employee.email,
            name = employee.name,
            password = "you don't get the password",
            birthDate = employee.birthDate,
            roles = employee.roles
        )
    }

    override fun getEmployee(email: String, password: String): EmployeeBoundary {
        val employeeHashKey = "employee:$email"
        val cachedEmployee = redisTemplate.opsForHash<String, String>().entries(employeeHashKey)

        if (cachedEmployee.isNotEmpty()) {
            val employeePassword = cachedEmployee["password"]
            if (employeePassword == password) {
                return mapToEmployeeBoundary(email, cachedEmployee)
            } else {
                throw EmployeeNotFoundException("Invalid credentials")
            }
        }

        throw EmployeeNotFoundException("Employee not found")
    }

    // Internal use for getByRole/domain/age without password check

    private fun mapToEmployeeBoundary(email: String, data: Map<String, String>): EmployeeBoundary {
        val name = data["name"]
        val roles = data["roles"]?.split(",") ?: listOf()

        val birthTimestampStr = data["birthTimestamp"]
        val birthDateInfo = birthTimestampStr?.let {
            val dateTime = LocalDateTime.parse(it)
            EmployeeBoundary.dateToDateInfo(dateTime)
        }

        return EmployeeBoundary(
            email = email,
            name = name,
            password = "you don't get the password",
            birthDate = birthDateInfo,
            roles = roles
        )
    }

    override fun getAll(page: Int, size: Int): List<EmployeeBoundary> {
        // Use Redis Sorted Sets for efficient pagination
        val start = page * size.toLong()
        val end = start + size - 1
        val employeeEmails = redisTemplate.opsForZSet().range("employeeByAge", start, end)

        // Ensure the correct function is used for fetching employee data
        return employeeEmails?.map { email -> getEmployeeByEmail(email) } ?: emptyList()
    }

    private fun getEmployeeByEmail(email: String): EmployeeBoundary {
        val employeeHashKey = "employee:$email"
        val cachedEmployee = redisTemplate.opsForHash<String, String>().entries(employeeHashKey)
        if (cachedEmployee.isEmpty()) throw EmployeeNotFoundException("Employee not found")
        return mapToEmployeeBoundary(email, cachedEmployee)
    }



    override fun getByDomain(domain: String, page: Int, size: Int): List<EmployeeBoundary> {
        // Normalize the domain and use Redis Set for filtering
        val normalizedDomain = if (domain.contains("@")) domain.split("@")[1] else domain
        val employeeEmails = redisTemplate.opsForSet().members("domain:$normalizedDomain")

        // Paginate the result if necessary
        return employeeEmails?.take(size)?.map { getEmployee(it, "") } ?: emptyList()
    }

    override fun getByRole(role: String, page: Int, size: Int): List<EmployeeBoundary> {
        // Use Redis Set for roles filtering
        val employees = redisTemplate.opsForSet().members("role:$role")
        return employees?.take(size)?.map { getEmployee(it, "") } ?: emptyList()
    }

    override fun getByAge(age: Int, page: Int, size: Int): List<EmployeeBoundary> {
        // Use Redis Sorted Set for efficient age-based range queries
        val now = LocalDateTime.now()
        val lowerBound = now.minusYears(age.toLong())
        val upperBound = lowerBound.plusYears(1).minusNanos(1)

        val employeeEmails = redisTemplate.opsForZSet().rangeByScore(
            "employeeByAge",
            lowerBound.toEpochSecond(ZoneOffset.UTC).toDouble(),
            upperBound.toEpochSecond(ZoneOffset.UTC).toDouble()
        )
        return employeeEmails?.take(size)?.map { getEmployee(it, "") } ?: emptyList()
    }

    override fun deleteAll() {
        // Use Redis keys to delete all entries in an optimized manner
        redisTemplate.connectionFactory?.let {
            val keys = redisTemplate.keys("*")
            redisTemplate.delete(keys)
        }
    }

    private fun validateEmployeeDetails(employee: EmployeeBoundary) {
        if (employee.password.isNullOrBlank() || !employee.password!!.contains(Regex("[A-Z]")) || !employee.password!!.contains(Regex("[0-9]"))) {
            throw InvalidInputException("Password must contain at least one uppercase letter and one digit")
        }
        if (employee.birthDate == null || employee.isDateEmpty(employee.birthDate!!)) {
            throw InvalidInputException("Birth date must not be null or empty")
        }
        if (employee.roles.isNullOrEmpty() || employee.roles!!.any { it.trim().isEmpty() }) {
            throw InvalidInputException("Roles must not be empty or contain blank entries")
        }
    }



}
