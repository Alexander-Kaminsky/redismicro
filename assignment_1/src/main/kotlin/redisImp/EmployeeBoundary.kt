package redisImp

import il.ac.afeka.cloud.WebMVCEmployees.DateInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class EmployeeBoundary (
    @field:NotBlank(message = "email cannot be empty")
    @field:Email(message = "Invalid email format")
    val email: String?,

    @field:NotBlank(message = "Name cannot be empty")
    //@field:Size(min = 1, message = "name must be at least 1 char long")
    val name: String?,

    @field:Size(min = 3, message = "password must be at least 3 char long")
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
        message = "Password must contain at least one uppercase letter and one digit"
    )
    var password:String?,

    @field:Valid
    var birthDate: DateInfo?,

    @field:NotEmpty(message = "roles cannot be empty")
    @field:Valid
    var roles: List<String>?){


    constructor(): this(null,null,null,null,null)

    constructor(entity: RedisEmployeeEntity):
            this(entity.email, entity.name, "you dont get the password", entity.birthTimestamp, entity.roles)

    constructor( name:String?, email:String?, createdTimestamp: LocalDateTime?, roles: List<String>):
            this(name,email,"you dont get the password",dateToDateInfo(createdTimestamp),roles)


    fun toRedisEntity(): RedisEmployeeEntity {
        requireNotNull(email) { "Email must not be null" }
        requireNotNull(name) { "Name must not be null" }
        requireNotNull(password) { "Password must not be null" }
        requireNotNull(birthDate) { "BirthDate must not be null" }
        require(!roles.isNullOrEmpty()) { "Roles must not be null or empty" }

        return RedisEmployeeEntity(
            email = email,
            name = name,
            password = password,
            birthTimestamp = birthDate,
            roles = roles!!
        )
    }

/*

    override fun toString(): String {
        return  "{" +
                "email:$email, " +
                "name:$name, " +
                "createdTimestamp:$birthDate" +
                "roles:$roles, " +
                "}"
    }
 */


    fun isDateEmpty(date: DateInfo): Boolean {
        return date.day == 0 && date.month == 0 && date.year == 0
    }

    companion object {
        fun dateToDateInfo(date: LocalDateTime?): DateInfo? {
            return date?.let {
                DateInfo(
                    day = it.dayOfMonth,
                    month = it.monthValue,
                    year = it.year
                )
            }
        }

        fun dateInfoToDate(dateInfo: DateInfo?): LocalDateTime? {
            return dateInfo?.let {
                LocalDateTime.of(it.year, it.month, it.day, 0, 0)
            }
        }
    }

}