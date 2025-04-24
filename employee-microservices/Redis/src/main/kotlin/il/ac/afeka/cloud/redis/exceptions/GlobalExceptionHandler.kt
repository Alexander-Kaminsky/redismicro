package il.ac.afeka.cloud.redis.exceptions

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException // Specific Redis exception
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
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

    // --- Specific Custom Exception Handlers ---
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException, request: WebRequest): ProblemDetail {
        log.warn("[Redis] Resource not found: {}", ex.message)
        return createProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.message)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException, request: WebRequest): ProblemDetail {
        log.warn("[Redis] Conflict detected: {}", ex.message)
        return createProblemDetail(HttpStatus.CONFLICT, "Conflict", ex.message)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException, request: WebRequest): ProblemDetail {
        log.warn("[Redis] Unauthorized access attempt: {}", ex.message)
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.message)
    }

    @ExceptionHandler(InvalidInputException::class, InvalidCriteriaException::class, IllegalArgumentException::class)
    fun handleBadRequestExceptions(ex: RuntimeException, request: WebRequest): ProblemDetail {
        log.warn("[Redis] Bad Request: {}", ex.message)
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.message)
    }


    // --- Validation Error Handlers (Identical Logic to projmvc) ---
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.warn("[Redis] Validation failed for request body: {}", ex.bindingResult.allErrors)
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        val problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Input validation failed for one or more fields.",
            mapOf("errors" to errors)
        )
        return ResponseEntity(problemDetail, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException, request: WebRequest): ProblemDetail {
        log.warn("[Redis] Constraint violation: {}", ex.constraintViolations)
        val errors = ex.constraintViolations.associate {
            it.propertyPath.toString().substringAfterLast('.') to (it.message ?: "Invalid value")
        }
        val problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Input validation failed for one or more parameters.",
            mapOf("errors" to errors)
        )
        return problemDetail
    }

    // --- Redis Specific Errors ---
    @ExceptionHandler(RedisConnectionFailureException::class)
    fun handleRedisConnectionFailure(ex: RedisConnectionFailureException, request: WebRequest): ProblemDetail {
        log.error("[Redis] Connection failure: {}", ex.message)
        return createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Service Unavailable",
            "Could not connect to the data store. Please try again later."
        )
    }

    // --- Generic Fallback Handler ---
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ProblemDetail {
        log.error("[Redis] An unexpected error occurred: {}", ex.message, ex)
        return createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please contact support."
        )
    }
}