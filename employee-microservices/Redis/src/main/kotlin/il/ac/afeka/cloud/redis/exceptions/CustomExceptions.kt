package il.ac.afeka.cloud.redis.exceptions

// Can potentially share these definitions with projmvc via a common module/library
class NotFoundException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
class InvalidInputException(message: String) : RuntimeException(message)
class InvalidCriteriaException(message: String) : RuntimeException(message)