package il.ac.afeka.cloud.redis.logic

import il.ac.afeka.cloud.redis.data.*
import il.ac.afeka.cloud.redis.exceptions.*
import il.ac.afeka.cloud.redis.persistence.RedisEmployeeEntity
import il.ac.afeka.cloud.redis.persistence.RedisEmployeeRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class RedisEmployeeServiceImpl(
    private val employeeRepository: RedisEmployeeRepository
) : RedisEmployeeService {

    private val logger = LoggerFactory.getLogger(RedisEmployeeServiceImpl::class.java)

    override fun createEmployee(employeeBoundary: EmployeeBoundary): EmployeeResponseBoundary {
        val email = employeeBoundary.email!!
        if (employeeRepository.existsById(email)) {
            logger.warn("Attempted to create employee with existing email: {}", email)
            throw ConflictException("Employee with email $email already exists.")
        }
        val employeeEntity = employeeBoundary.toRedisEntity()
        val savedEntity = employeeRepository.save(employeeEntity)
        logger.info("Created employee in Redis with email: {}", savedEntity.email)
        return savedEntity.toResponseBoundary()
    }

    override fun getEmployeeByEmailAndPassword(email: String, passwordAttempt: String): EmployeeResponseBoundary {
        val employee = employeeRepository.findById(email)
            .orElseThrow {
                logger.warn("Employee not found in Redis for login attempt: {}", email)
                NotFoundException("Employee with email $email not found.")
            }

        // WARNING: Direct password comparison - INSECURE!
        if (employee.password != passwordAttempt) {
            logger.warn("Incorrect password attempt for email in Redis: {}", email)
            throw UnauthorizedException("Incorrect password for employee $email.")
        }

        logger.debug("Successfully authenticated employee from Redis: {}", email)
        return employee.toResponseBoundary()
    }

    override fun getAllEmployees(pageable: Pageable): Page<EmployeeResponseBoundary> {
        logger.debug("Fetching all employees from Redis with pagination: {}", pageable)
        return employeeRepository
            .findAll(pageable)
            .map(RedisEmployeeEntity::toResponseBoundary)
    }

    override fun getEmployeesByCriteria(criteria: String, value: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        logger.debug("Fetching employees from Redis by criteria '{}'='{}' with pagination: {}", criteria, value, pageable)
        return when (criteria.lowercase()) {
            "byemaildomain" -> findByEmailDomain(value, pageable) // Filter in memory
            "byrole" -> findByRole(value, pageable) // Uses repository method if indexed
            "byage" -> findByAge(value, pageable) // Filter in memory
            else -> {
                logger.warn("Invalid criteria specified for Redis: {}", criteria)
                throw InvalidCriteriaException("Invalid criteria specified: $criteria. Valid criteria are: byEmailDomain, byRole, byAge.")
            }
        }
    }

    private fun findByRole(role: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        // This relies on the @Indexed annotation on the 'roles' field and the repository method
        if (role.isBlank()) throw InvalidCriteriaException("Role value cannot be blank.")
        return employeeRepository.findByRolesContains(role, pageable).map(RedisEmployeeEntity::toResponseBoundary)
        // If not indexed or method doesn't work as expected, fallback to in-memory filtering:
        // return filterInMemory(pageable) { it.roles.contains(role) }
    }

    // --- In-Memory Filtering Methods (Less Efficient for Large Datasets) ---

    private fun findByEmailDomain(domain: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        if (domain.isBlank() || !domain.contains('.')) throw InvalidCriteriaException("Invalid domain format provided.")
        return filterInMemory(pageable) {
            it.email.substringAfterLast('@', "").equals(domain, ignoreCase = true)
        }
    }

    private fun findByAge(ageInYearsString: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        val ageInYears = ageInYearsString.toIntOrNull()
            ?: throw InvalidCriteriaException("Invalid age format: '$ageInYearsString'. Age must be an integer.")
        if (ageInYears < 0) {
            throw InvalidCriteriaException("Age cannot be negative: $ageInYears")
        }
        val today = LocalDate.now()

        return filterInMemory(pageable) {
            val birthDate = it.getBirthdateAsLocalDate()
            val age = Period.between(birthDate, today).years
            age == ageInYears
        }
    }

    // Helper for filtering all data fetched from Redis (use with caution on large datasets)
    private fun filterInMemory(pageable: Pageable, predicate: (RedisEmployeeEntity) -> Boolean): Page<EmployeeResponseBoundary> {
        logger.warn("Performing in-memory filtering for Redis query - may be inefficient.")
        val allEmployees = employeeRepository.findAll().toList() // Fetches ALL data!
        val filtered = allEmployees.filter(predicate)
        return paginateList(filtered, pageable).map(RedisEmployeeEntity::toResponseBoundary)
    }

    // Helper for manual pagination of in-memory lists
    private fun <T> paginateList(list: List<T>, pageable: Pageable): Page<T> {
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(list.size)

        return if (start >= list.size) {
            PageImpl(emptyList(), pageable, list.size.toLong())
        } else {
            PageImpl(list.subList(start, end), pageable, list.size.toLong())
        }
    }
    // --- End In-Memory Filtering ---


    override fun deleteAllEmployees() {
        logger.warn("Deleting all employee data from Redis.")
        employeeRepository.deleteAll()
    }

    // --- Bonus Method Implementations ---

    override fun assignManager(employeeEmail: String, managerEmailBoundary: ManagerEmailBoundary) {
        val managerEmail = managerEmailBoundary.email!!
        if (employeeEmail.equals(managerEmail, ignoreCase = true)) {
            throw InvalidInputException("Employee cannot be their own manager.")
        }

        // Fetch both - ensure they exist
        val employee = employeeRepository.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee with email $employeeEmail not found in Redis.") }
        val manager = employeeRepository.findById(managerEmail) // Check existence only
            .orElseThrow { NotFoundException("Manager with email $managerEmail not found in Redis.") }

        // Simplified cycle check - check if the potential manager's manager is the employee
        if (manager.managerEmail?.equals(employee.email, ignoreCase = true) == true) {
            throw ConflictException("Circular manager assignment detected.")
        }

        if (employee.managerEmail?.equals(manager.email, ignoreCase = true) == true) {
            logger.debug("Manager {} already assigned to employee {} in Redis", managerEmail, employeeEmail)
            return
        }

        // Update the employee's managerEmail field and save
        val updatedEmployee = employee.copy(managerEmail = manager.email)
        employeeRepository.save(updatedEmployee)
        logger.info("Assigned manager {} to employee {} in Redis", managerEmail, employeeEmail)
    }

    override fun getManager(employeeEmail: String): EmployeeResponseBoundary {
        val employee = employeeRepository.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee with email $employeeEmail not found in Redis.") }

        val managerEmail = employee.managerEmail
            ?: throw NotFoundException("No manager defined for employee $employeeEmail in Redis.")

        // Fetch the manager entity itself
        val manager = employeeRepository.findById(managerEmail)
            .orElseThrow {
                logger.error("Data inconsistency in Redis: Manager {} not found for employee {}", managerEmail, employeeEmail)
                NotFoundException("Manager with email $managerEmail not found in Redis (data inconsistency).")
            }

        return manager.toResponseBoundary()
    }

    override fun getSubordinates(managerEmail: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        // Check if manager exists (optional, but good practice)
        if (!employeeRepository.existsById(managerEmail)) {
            logger.warn("Attempted to get subordinates for non-existent manager in Redis: {}", managerEmail)
            return Page.empty(pageable) // Return empty as per spec
        }

        // This relies on the @Indexed annotation on managerEmail and the repository method
        return employeeRepository.findByManagerEmail(managerEmail, pageable)
            .map(RedisEmployeeEntity::toResponseBoundary)
        // If not indexed, fallback to in-memory filtering:
        // return filterInMemory(pageable) { it.managerEmail == managerEmail }
    }

    override fun removeManager(employeeEmail: String) {
        val employee = employeeRepository.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee with email $employeeEmail not found in Redis.") }

        if (employee.managerEmail == null) {
            logger.debug("No manager to remove for employee {} in Redis", employeeEmail)
            return
        }

        val updatedEmployee = employee.copy(managerEmail = null)
        employeeRepository.save(updatedEmployee)
        logger.info("Removed manager assignment for employee {} in Redis", employeeEmail)
    }
}