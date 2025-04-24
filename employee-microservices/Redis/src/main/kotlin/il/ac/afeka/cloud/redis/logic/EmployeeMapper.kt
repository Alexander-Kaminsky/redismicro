package il.ac.afeka.cloud.redis.logic

import il.ac.afeka.cloud.redis.data.BirthdateBoundary
import il.ac.afeka.cloud.redis.data.EmployeeBoundary
import il.ac.afeka.cloud.redis.data.EmployeeResponseBoundary
import il.ac.afeka.cloud.redis.exceptions.InvalidInputException
import il.ac.afeka.cloud.redis.persistence.RedisEmployeeEntity
import java.time.format.DateTimeFormatter

// Simple Mapper functions (Consider MapStruct for complex projects)

fun RedisEmployeeEntity.toResponseBoundary(): EmployeeResponseBoundary {
    return EmployeeResponseBoundary(
        email = this.email,
        name = this.name,
        birthdate = BirthdateBoundary.fromLocalDate(this.getBirthdateAsLocalDate()),
        roles = this.roles.toList().sorted()
    )
}

fun EmployeeBoundary.toRedisEntity(): RedisEmployeeEntity {
    val localDate = this.birthdate?.toLocalDate()
        ?: throw InvalidInputException("Invalid birthdate format provided.")

    return RedisEmployeeEntity(
        email = this.email!!, // Validation ensures not null
        name = this.name!!,
        // WARNING: Storing plain text password!
        password = this.password!!,
        birthdate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE), // Store as YYYY-MM-DD string
        roles = this.roles!!.toSet(), // Use Set for roles
        managerEmail = null // Manager set separately
    )
}