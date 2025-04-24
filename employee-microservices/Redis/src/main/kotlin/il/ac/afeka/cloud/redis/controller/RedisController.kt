package il.ac.afeka.cloud.redis.controller

import il.ac.afeka.cloud.redis.data.*
import il.ac.afeka.cloud.redis.exceptions.InvalidInputException // Use correct exception import
import il.ac.afeka.cloud.redis.logic.RedisEmployeeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees") // Using the same base path, ensure ports are different!
@Validated
@Tag(name = "Employee Management (Redis)", description = "APIs for managing employee data using Redis")
class RedisController(private val employeeService: RedisEmployeeService) { // Inject Redis service

    private val logger = LoggerFactory.getLogger(RedisController::class.java)
    // Default sort by email key in Redis
    private val defaultSort = Sort.by(Sort.Direction.ASC, "email")

    // --- Endpoint implementations are identical to EmployeeController in projmvc ---
    // --- Just replace employeeService calls and logging messages slightly ---

    @Operation(summary = "Create a new employee (Redis)") // Add Redis context
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createEmployee(@Valid @RequestBody employee: EmployeeBoundary): ResponseEntity<EmployeeResponseBoundary> {
        logger.info("[Redis] Received request to create employee: {}", employee.email)
        val createdEmployee = employeeService.createEmployee(employee)
        return ResponseEntity(createdEmployee, HttpStatus.CREATED)
    }

    @Operation(summary = "Get employee by email and password (Redis)")
    @GetMapping(
        "/{employeeEmail}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getEmployeeByEmailAndPassword(
        @PathVariable @Email(message = "Invalid email format in path") employeeEmail: String,
        @RequestParam @NotBlank(message = "Password parameter is required") password: String
    ): ResponseEntity<EmployeeResponseBoundary> {
        logger.debug("[Redis] Received request to get employee by email: {}", employeeEmail)
        val employee = employeeService.getEmployeeByEmailAndPassword(employeeEmail, password)
        return ResponseEntity.ok(employee)
    }

    @Operation(summary = "Get employees with filtering and pagination (Redis)")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEmployees(
        @RequestParam(required = false) criteria: String?,
        @RequestParam(required = false) value: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) size: Int
    ): ResponseEntity<Page<EmployeeResponseBoundary>> {
        logger.debug("[Redis] Received request to get employees. Criteria: {}, Value: {}, Page: {}, Size: {}", criteria, value, page, size)
        // Note: Redis sort might behave differently than SQL sort depending on implementation
        val pageable = PageRequest.of(page, size, defaultSort)

        val results = if (criteria != null && value != null) {
            employeeService.getEmployeesByCriteria(criteria, value, pageable)
        } else if (criteria == null && value == null) {
            employeeService.getAllEmployees(pageable)
        } else {
            throw InvalidInputException("Both 'criteria' and 'value' parameters must be provided together for filtering, or neither for fetching all.")
        }
        return ResponseEntity.ok(results)
    }

    @Operation(summary = "Delete all employees (Redis)")
    @DeleteMapping
    fun deleteAllEmployees(): ResponseEntity<Void> {
        logger.warn("[Redis] Received request to DELETE ALL employees.")
        employeeService.deleteAllEmployees()
        return ResponseEntity.noContent().build()
    }

    // --- Bonus Endpoints (Redis) ---

    @Operation(summary = "Assign a manager to an employee (Redis)")
    @PutMapping(
        "/{employeeEmail}/manager",
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun assignManager(
        @PathVariable @Email(message = "Invalid employee email format in path") employeeEmail: String,
        @Valid @RequestBody managerEmailBoundary: ManagerEmailBoundary
    ): ResponseEntity<Void> {
        logger.info("[Redis] Received request to assign manager {} to employee {}", managerEmailBoundary.email, employeeEmail)
        employeeService.assignManager(employeeEmail, managerEmailBoundary)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Get an employee's manager (Redis)")
    @GetMapping(
        "/{employeeEmail}/manager",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getManager(
        @PathVariable @Email(message = "Invalid employee email format in path") employeeEmail: String
    ): ResponseEntity<EmployeeResponseBoundary> {
        logger.debug("[Redis] Received request to get manager for employee {}", employeeEmail)
        val manager = employeeService.getManager(employeeEmail)
        return ResponseEntity.ok(manager)
    }

    @Operation(summary = "Get a manager's direct subordinates (Redis)")
    @GetMapping(
        "/{managerEmail}/subordinates",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getSubordinates(
        @PathVariable @Email(message = "Invalid manager email format in path") managerEmail: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) size: Int
    ): ResponseEntity<Page<EmployeeResponseBoundary>> {
        logger.debug("[Redis] Received request to get subordinates for manager {}. Page: {}, Size: {}", managerEmail, page, size)
        val pageable = PageRequest.of(page, size, defaultSort)
        val subordinates = employeeService.getSubordinates(managerEmail, pageable)
        return ResponseEntity.ok(subordinates)
    }

    @Operation(summary = "Remove manager assignment from an employee (Redis)")
    @DeleteMapping("/{employeeEmail}/manager")
    fun removeManager(
        @PathVariable @Email(message = "Invalid employee email format in path") employeeEmail: String
    ): ResponseEntity<Void> {
        logger.info("[Redis] Received request to remove manager for employee {}", employeeEmail)
        employeeService.removeManager(employeeEmail)
        return ResponseEntity.noContent().build()
    }
}