package il.ac.afeka.cloud.projmvc.exceptions

// Specific exception types for better error handling granularity

class NotFoundException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
class InvalidInputException(message: String) : RuntimeException(message) // General invalid user input
class InvalidCriteriaException(message: String) : RuntimeException(message) // Specific to bad criteria/value
// InvalidEmailException might be too granular, covered by @Email validation or InvalidInputException