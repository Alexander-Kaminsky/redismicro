package redisImp

import il.ac.afeka.cloud.WebMVCEmployees.EmployeeNotFoundException
import il.ac.afeka.cloud.WebMVCEmployees.InvalidCriteriaException
import il.ac.afeka.cloud.WebMVCEmployees.InvalidInputException
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = [ "/employees"])
class RedisController(
    val employeeService: RedisEmployeeService
) {

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun create(@Valid @RequestBody newEmployee: EmployeeBoundary): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(employeeService.createEmployee(newEmployee))
        } catch (e: InvalidInputException) {
            ResponseEntity.badRequest().body(
                mapOf("error" to "Invalid Input", "message" to e.message)
            )
        }
    }


    @GetMapping(
        path = ["/{employeeEmail}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getByEmailAndPassword(
        @PathVariable("employeeEmail") email: String,
        @RequestParam("password") password: String
    ): ResponseEntity<Any> {
        return try {
            val employee = employeeService.getEmployee(email, password)
            ResponseEntity.ok(employee)
        } catch (e: EmployeeNotFoundException) {
            ResponseEntity.status(404).body(
                mapOf("error" to "Employee Not Found", "message" to e.message)
            )
        }
    }

    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        params = ["!criteria", "!value"]
    )
    fun getAll(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "5") size: Int
    ): ResponseEntity<List<EmployeeBoundary>> {
        val employees = employeeService.getAll(page, size)
        return ResponseEntity.ok(employees)
    }

    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        params = ["criteria", "value"]
    )
    fun getByCriteria(
        @RequestParam("criteria", required = false) criteria: String?,
        @RequestParam("value", required = false) value: String?,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "5") size: Int
    ): ResponseEntity<List<EmployeeBoundary>> {
        return try {
            val employees = when (criteria) {
                "byEmailDomain" -> employeeService.getByDomain(value.orEmpty(), page, size)
                "byRole" -> employeeService.getByRole(value.orEmpty(), page, size)
                "byAge" -> {
                    val age = value?.toIntOrNull()
                        ?: throw IllegalArgumentException("Age must be a valid number")
                    employeeService.getByAge(age, page, size)
                }
                else -> throw InvalidCriteriaException("Invalid criteria provided")
            }
            ResponseEntity.ok(employees)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(emptyList())
        } catch (e: InvalidCriteriaException) {
            ResponseEntity.badRequest().body(emptyList())
        }
    }

    @DeleteMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteAll(): ResponseEntity<Void> {
        return try {
            employeeService.deleteAll()
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }
}