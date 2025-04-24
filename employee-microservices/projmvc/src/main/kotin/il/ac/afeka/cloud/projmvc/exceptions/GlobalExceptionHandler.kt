package il.ac.afeka.cloud.projmvc.exceptions

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException // Catch JPA/Database access issues
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail // Use ProblemDetail for RFC 7807 compliance
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun createProblemDetail(status: HttpStatus, title: String, detail: String?, properties: Map<String, Any>? = null): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(status)
        problemDetail.title = title
        if (detail != null) {
            problemDetail.detail = detail
        }
        properties?.forEach { (key, value) -> problemDetail.setProperty(key, value) }
        return problemDetail
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException, request: WebRequest): ProblemDetail {
        log.warn("Resource not found: {}", ex.message)
        return createProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.message)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException, request: WebRequest): ProblemDetail {
        log.warn("Conflict detected: {}", ex.message)
        return createProblemDetail(HttpStatus.CONFLICT, "Conflict", ex.message)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException, request: WebRequest): ProblemDetail {
        log.warn("Unauthorized access attempt: {}", ex.message)
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.message)
    }

    @ExceptionHandler(InvalidInputException::class, InvalidCriteriaException::class, IllegalArgumentException::class)
    fun handleBadRequestExceptions(ex: RuntimeException, request: WebRequest): ProblemDetail {
        log.warn("Bad Request: {}", ex.message)
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.message)
    }

    // --- Handle Validation Errors ---

    // Handles @Valid errors on @RequestBody
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.warn("Validation failed for request body: {}", ex.bindingResult.allErrors)
        val errors = ex.bindingResult.fieldErrors.associate {
            // Provide more context if available (e.g., rejected value)
            val field = it.field
            val message = it.defaultMessage ?: "Invalid value"
            field to message
        }
        val problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Input validation failed for one or more fields.",
            mapOf("errors" to errors)
        )
        return ResponseEntity(problemDetail, HttpStatus.BAD_REQUEST)
    }

    // Handles validation errors on @RequestParam, @PathVariable (needs @Validated on controller class)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException, request: WebRequest): ProblemDetail {
        log.warn("Constraint violation: {}", ex.constraintViolations)
        val errors = ex.constraintViolations.associate {
            // Try to get a cleaner field name
            val propertyPath = it.propertyPath.toString()
            val fieldName = propertyPath.substringAfterLast('.') // Simple heuristic
            fieldName to (it.message ?: "Invalid value")
        }
        val problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Input validation failed for one or more parameters.",
            mapOf("errors" to errors)
        )
        return problemDetail
    }

    // --- Handle Database/JPA Errors ---
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(ex: DataAccessException, request: WebRequest): ProblemDetail {
        log.error("Database access error: {}", ex.message, ex) // Log underlying cause
        // Avoid exposing detailed DB errors to the client
        return createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Database Error",
            "A database error occurred. Please try again later."
        )
    }


    // --- Generic Fallback Handler ---
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ProblemDetail {
        log.error("An unexpected error occurred: {}", ex.message, ex) // Log stack trace
        return createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please contact support."
        )
    }
}