package il.ac.afeka.cloud.projmvc.controller

import il.ac.afeka.cloud.projmvc.data.*
import il.ac.afeka.cloud.projmvc.exceptions.InvalidInputException
import il.ac.afeka.cloud.projmvc.logic.EmployeeService
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
@RequestMapping("/employees")
@Validated // Enable validation for path/query parameters
@Tag(name = "Employee Management (JPA)", description = "APIs for managing employee data using PostgreSQL")
class EmployeeController(private val employeeService: EmployeeService) {

    private val logger = LoggerFactory.getLogger(EmployeeController::class.java)
    private val defaultSort = Sort.by(Sort.Direction.ASC, "email") // Default sort order

    @Operation(
        summary = "Create a new employee",
        description = "Adds a new employee record to the database. Email must be unique.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Employee details",
            required = true,
            content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = EmployeeBoundary::class))]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "Employee created successfully", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = EmployeeResponseBoundary::class))]),
            ApiResponse(responseCode = "400", description = "Invalid input data", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            ApiResponse(responseCode = "409", description = "Employee with this email already exists", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        ]
    )
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createEmployee(@Valid @RequestBody employee: EmployeeBoundary): ResponseEntity<EmployeeResponseBoundary> {
        logger.info("Received request to create employee: {}", employee.email)
        val createdEmployee = employeeService.createEmployee(employee)
        return ResponseEntity(createdEmployee, HttpStatus.CREATED) // Return 201 Created
    }

    @Operation(
        summary = "Get employee by email and password",
        description = "Retrieves specific employee details if email and password match. Password is NOT included in the response.",
        parameters = [
            Parameter(name = "employeeEmail", description = "Employee's email address", required = true, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
            Parameter(name = "password", description = "Employee's password", required = true, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Employee details found", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = EmployeeResponseBoundary::class))]),
            ApiResponse(responseCode = "401", description = "Incorrect password", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            ApiResponse(responseCode = "404", description = "Employee not found", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        ]
    )
    @GetMapping(
        "/{employeeEmail}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getEmployeeByEmailAndPassword(
        @PathVariable @Email(message = "Invalid email format in path") employeeEmail: String,
        @RequestParam @NotBlank(message = "Password parameter is required") password: String // Added validation
    ): ResponseEntity<EmployeeResponseBoundary> {
        logger.debug("Received request to get employee by email: {}", employeeEmail)
        // WARNING: Passing password as query param is insecure!
        val employee = employeeService.getEmployeeByEmailAndPassword(employeeEmail, password)
        return ResponseEntity.ok(employee) // Return 200 OK
    }

    @Operation(
        summary = "Get employees with filtering and pagination",
        description = "Retrieves a paginated list of employees. Can optionally filter by criteria (byEmailDomain, byRole, byAge).",
        parameters = [
            Parameter(name = "criteria", description = "Filtering criteria (byEmailDomain, byRole, byAge)", required = false, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            Parameter(name = "value", description = "Value for the criteria (e.g., 'example.com', 'Admin', '30')", required = false, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            Parameter(name = "page", description = "Page number (0-based)", required = false, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY, schema = Schema(defaultValue = "0")),
            Parameter(name = "size", description = "Page size", required = false, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY, schema = Schema(defaultValue = "10"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "List of employees retrieved", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]), // Schema would be Page<EmployeeResponseBoundary>
            ApiResponse(responseCode = "400", description = "Invalid criteria, value, page, or size", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        ]
    )
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEmployees(
        @RequestParam(required = false) criteria: String?,
        @RequestParam(required = false) value: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) size: Int
    ): ResponseEntity<Page<EmployeeResponseBoundary>> {
        logger.debug("Received request to get employees. Criteria: {}, Value: {}, Page: {}, Size: {}", criteria, value, page, size)
        val pageable = PageRequest.of(page, size, defaultSort)

        val results = if (criteria != null && value != null) {
            employeeService.getEmployeesByCriteria(criteria, value, pageable)
        } else if (criteria == null && value == null) {
            employeeService.getAllEmployees(pageable)
        } else {
            // Handle cases where only one of criteria/value is provided
            logger.warn("Invalid combination of criteria/value parameters. Criteria: {}, Value: {}", criteria, value)
            // Throwing exception is better handled by GlobalExceptionHandler
            throw InvalidInputException("Both 'criteria' and 'value' parameters must be provided together for filtering, or neither for fetching all.")
        }
        return ResponseEntity.ok(results)
    }

    @Operation(
        summary = "Delete all employees",
        description = "WARNING: This operation removes all employee records from the database.",
        responses = [
            ApiResponse(responseCode = "204", description = "All employees deleted successfully")
        ]
    )
    @DeleteMapping
    fun deleteAllEmployees(): ResponseEntity<Void> {
        logger.warn("Received request to DELETE ALL employees (PostgreSQL).")
        employeeService.deleteAllEmployees()
        return ResponseEntity.noContent().build() // Return 204 No Content
    }

    // --- Bonus Endpoints ---

    @Operation(
        summary = "Assign a manager to an employee",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON object containing the manager's email",
            required = true,
            content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ManagerEmailBoundary::class))]
        ),
        parameters = [
            Parameter(name = "employeeEmail", description = "Email of the employee to assign manager to", required = true, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Manager assigned successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input (e.g., employee is own manager, invalid email)", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            ApiResponse(responseCode = "404", description = "Employee or Manager not found", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            ApiResponse(responseCode = "409", description = "Circular dependency detected", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        ]
    )
    @PutMapping(
        "/{employeeEmail}/manager",
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun assignManager(
        @PathVariable @Email(message = "Invalid employee email format in path") employeeEmail: String,
        @Valid @RequestBody managerEmailBoundary: ManagerEmailBoundary
    ): ResponseEntity<Void> {
        logger.info("Received request to assign manager {} to employee {}", managerEmailBoundary.email, employeeEmail)
        employeeService.assignManager(employeeEmail, managerEmailBoundary)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "Get an employee's manager",
        parameters = [
            Parameter(name = "employeeEmail", description = "Email of the employee whose manager to retrieve", required = true, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Manager details retrieved", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = EmployeeResponseBoundary::class))]),
            ApiResponse(responseCode = "404", description = "Employee not found or no manager assigned", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        ]
    )
    @GetMapping(
        "/{employeeEmail}/manager",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getManager(
        @PathVariable @Email(message = "Invalid employee email format in path") employeeEmail: String
    ): ResponseEntity<EmployeeResponseBoundary> {
        logger.debug("Received request to get manager for employee {}", employeeEmail)
        val manager = employeeService.getManager(employeeEmail)
        return ResponseEntity.ok(manager)
    }

    @Operation(
        summary = "Get a manager's direct subordinates",
        description = "Retrieves a paginated list of employees who report directly to the specified manager.",
        parameters = [
            Parameter(name = "managerEmail", description = "Email of the manager", required = true, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
            Parameter(name = "page", description = "Page number (0-based)", required = false, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY, schema = Schema(defaultValue = "0")),
            Parameter(name = "size", description = "Page size", required = false, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY, schema = Schema(defaultValue = "10"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "List of subordinates retrieved (can be empty)", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]) // Schema Page<EmployeeResponseBoundary>
            // 404 for manager not found is handled by returning empty list as per spec
        ]
    )
    @GetMapping(
        "/{managerEmail}/subordinates",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getSubordinates(
        @PathVariable @Email(message = "Invalid manager email format in path") managerEmail: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) size: Int
    ): ResponseEntity<Page<EmployeeResponseBoundary>> {
        logger.debug("Received request to get subordinates for manager {}. Page: {}, Size: {}", managerEmail, page, size)
        val pageable = PageRequest.of(page, size, defaultSort) // Sort subordinates by email
        val subordinates = employeeService.getSubordinates(managerEmail, pageable)
        return ResponseEntity.ok(subordinates)
    }

    @Operation(
        summary = "Remove manager assignment from an employee",
        parameters = [
            Parameter(name = "employeeEmail", description = "Email of the employee whose manager assignment to remove", required = true, `in` = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "Manager assignment removed successfully (or employee had no manager)"),
            ApiResponse(responseCode = "404", description = "Employee not found", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        ]
    )
    @DeleteMapping("/{employeeEmail}/manager")
    fun removeManager(
        @PathVariable @Email(message = "Invalid employee email format in path") employeeEmail: String
    ): ResponseEntity<Void> {
        logger.info("Received request to remove manager for employee {}", employeeEmail)
        employeeService.removeManager(employeeEmail)
        return ResponseEntity.noContent().build() // Return 204 No Content
    }
}