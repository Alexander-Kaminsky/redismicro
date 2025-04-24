package il.ac.afeka.cloud.projmvc.logic

import il.ac.afeka.cloud.projmvc.data.*
import il.ac.afeka.cloud.projmvc.exceptions.*
import il.ac.afeka.cloud.projmvc.persistence.EmployeeCrud
import il.ac.afeka.cloud.projmvc.persistence.EmployeeEntity
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Period

@Service
class EmployeeServiceImpl(private val employeeRepository: EmployeeCrud) : EmployeeService {

    private val logger = LoggerFactory.getLogger(EmployeeServiceImpl::class.java)

    @Transactional // Make creation atomic
    override fun createEmployee(employeeBoundary: EmployeeBoundary): EmployeeResponseBoundary {
        val email = employeeBoundary.email!! // Validation ensures not null
        if (employeeRepository.existsById(email)) {
            logger.warn("Attempted to create employee with existing email: {}", email)
            throw ConflictException("Employee with email $email already exists.")
        }

        val birthDate = employeeBoundary.birthdate!!.toLocalDate()
            ?: throw InvalidInputException("Invalid birthdate format provided.") // Should be caught by @ValidDate

        val employeeEntity = EmployeeEntity(
            email = email,
            name = employeeBoundary.name!!,
            // WARNING: Storing plain text password as per requirement. HASH in production!
            password = employeeBoundary.password!!,
            birthdate = birthDate, // Store as LocalDate
            roles = employeeBoundary.roles!!.toMutableSet()
        )

        val savedEntity = employeeRepository.save(employeeEntity)
        logger.info("Created employee with email: {}", savedEntity.email)
        return savedEntity.toResponseBoundary()
    }

    @Transactional(readOnly = true) // Good practice for read operations
    override fun getEmployeeByEmailAndPassword(email: String, passwordAttempt: String): EmployeeResponseBoundary {
        val employee = employeeRepository.findById(email)
            .orElseThrow {
                logger.warn("Employee not found for login attempt: {}", email)
                NotFoundException("Employee with email $email not found.")
            }

        // WARNING: Direct password comparison - INSECURE! Use hashing in production.
        if (employee.password != passwordAttempt) {
            logger.warn("Incorrect password attempt for email: {}", email)
            throw UnauthorizedException("Incorrect password for employee $email.")
        }

        logger.debug("Successfully authenticated employee: {}", email)
        return employee.toResponseBoundary()
    }

    @Transactional(readOnly = true)
    override fun getAllEmployees(pageable: Pageable): Page<EmployeeResponseBoundary> {
        logger.debug("Fetching all employees with pagination: {}", pageable)
        return employeeRepository
            .findAll(pageable)
            .map(EmployeeEntity::toResponseBoundary) // Convert each entity
    }

    @Transactional(readOnly = true)
    override fun getEmployeesByCriteria(criteria: String, value: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        logger.debug("Fetching employees by criteria '{}'='{}' with pagination: {}", criteria, value, pageable)
        return when (criteria.lowercase()) {
            "byemaildomain" -> {
                if (value.isBlank() || !value.contains('.')) throw InvalidCriteriaException("Invalid domain format provided.")
                employeeRepository.findByEmailDomain(value.lowercase(), pageable).map(EmployeeEntity::toResponseBoundary)
            }
            "byrole" -> {
                if (value.isBlank()) throw InvalidCriteriaException("Role value cannot be blank.")
                employeeRepository.findByRolesContains(value, pageable).map(EmployeeEntity::toResponseBoundary)
            }
            "byage" -> findByAge(value, pageable)
            else -> {
                logger.warn("Invalid criteria specified: {}", criteria)
                throw InvalidCriteriaException("Invalid criteria specified: $criteria. Valid criteria are: byEmailDomain, byRole, byAge.")
            }
        }
    }

    private fun findByAge(ageInYearsString: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        val ageInYears = ageInYearsString.toIntOrNull()
            ?: throw InvalidCriteriaException("Invalid age format: '$ageInYearsString'. Age must be an integer.")

        if (ageInYears < 0) {
            throw InvalidCriteriaException("Age cannot be negative: $ageInYears")
        }

        // Calculate the date range for the given age
        // Example: For age 30 today (April 19, 2025), find birthdays between April 20, 1994 and April 19, 1995.
        val today = LocalDate.now()
        val endDate = today.minusYears(ageInYears.toLong()) // Latest possible birthdate (inclusive)
        val startDate = today.minusYears((ageInYears + 1).toLong()).plusDays(1) // Earliest possible birthdate (inclusive)

        logger.debug("Searching for age {} between dates {} and {}", ageInYears, startDate, endDate)

        return employeeRepository.findByBirthdateBetween(startDate, endDate, pageable)
            .map(EmployeeEntity::toResponseBoundary)
    }


    @Transactional // Destructive operation
    override fun deleteAllEmployees() {
        logger.warn("Deleting all employee data from PostgreSQL.")
        // Consider implications: deletes roles and potentially breaks manager links if not handled carefully
        employeeRepository.deleteAll()
    }

    // --- Bonus Method Implementations ---

    @Transactional
    override fun assignManager(employeeEmail: String, managerEmailBoundary: ManagerEmailBoundary) {
        val managerEmail = managerEmailBoundary.email!! // Validation ensures not null

        if (employeeEmail.equals(managerEmail, ignoreCase = true)) {
            throw InvalidInputException("Employee cannot be their own manager.")
        }

        // Fetch both employee and potential manager
        val employee = employeeRepository.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee with email $employeeEmail not found.") }
        val manager = employeeRepository.findById(managerEmail)
            .orElseThrow { NotFoundException("Manager with email $managerEmail not found.") }

        // Check for circular dependency (simplified check: manager's manager is not the employee)
        if (manager.manager?.email?.equals(employee.email, ignoreCase = true) == true) {
            throw ConflictException("Circular manager assignment detected.")
        }
        // Deeper cycle detection might be needed for complex hierarchies

        // Check if assignment is already correct
        if (employee.manager?.email?.equals(manager.email, ignoreCase = true) == true) {
            logger.debug("Manager {} already assigned to employee {}", managerEmail, employeeEmail)
            return // No change needed
        }

        employee.manager = manager // Assign the manager entity
        employeeRepository.save(employee)
        logger.info("Assigned manager {} to employee {}", managerEmail, employeeEmail)
    }

    @Transactional(readOnly = true)
    override fun getManager(employeeEmail: String): EmployeeResponseBoundary {
        val employee = employeeRepository.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee with email $employeeEmail not found.") }

        val manager = employee.manager
            ?: throw NotFoundException("No manager defined for employee $employeeEmail.")

        // The manager object is already fetched (potentially lazily), return its boundary
        return manager.toResponseBoundary()
    }

    @Transactional(readOnly = true)
    override fun getSubordinates(managerEmail: String, pageable: Pageable): Page<EmployeeResponseBoundary> {
        // Check if the manager actually exists
        if (!employeeRepository.existsById(managerEmail)) {
            logger.warn("Attempted to get subordinates for non-existent manager: {}", managerEmail)
            return Page.empty(pageable) // Return empty page as per requirement
        }

        return employeeRepository.findByManagerEmail(managerEmail, pageable)
            .map(EmployeeEntity::toResponseBoundary)
    }

    @Transactional
    override fun removeManager(employeeEmail: String) {
        val employee = employeeRepository.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee with email $employeeEmail not found.") }

        if (employee.manager == null) {
            logger.debug("No manager to remove for employee {}", employeeEmail)
            return // Nothing to do
        }

        employee.manager = null // Remove the association
        employeeRepository.save(employee)
        logger.info("Removed manager assignment for employee {}", employeeEmail)
    }

    // --- Helper Extension Function ---
    fun EmployeeEntity.toResponseBoundary(): EmployeeResponseBoundary {
        // Handle conversion based on how birthdate is stored
        val bdBoundary = BirthdateBoundary.fromLocalDate(this.birthdate)
        // If using @Embedded BirthdateBoundary:
        // val bdBoundary = this.birthdateBoundary

        return EmployeeResponseBoundary(
            email = this.email,
            name = this.name,
            birthdate = bdBoundary,
            roles = this.roles.toList().sorted() // Return sorted list
        )
    }
}