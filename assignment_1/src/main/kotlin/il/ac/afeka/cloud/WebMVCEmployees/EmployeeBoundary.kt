package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDateTime


class EmployeeBoundary (
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

    constructor(entity:EmployeeEntity):
            this(entity.email,entity.name,"you dont get the password",dateToDateInfo(entity.birthTimestamp),stringToRoles(entity.roles))

    constructor(email:String?,name:String?, createdTimestamp: LocalDateTime?, roles: String?):
            this(email,name,"you dont get the password",dateToDateInfo(createdTimestamp),stringToRoles(roles))

    fun toEntity(): EmployeeEntity {
        val rv = EmployeeEntity()
        try {
            println("Converting to entity: $this")
            rv.email = this.email
            rv.name = this.name ?: "Unnamed"
            rv.passwordHash = this.password
            rv.birthTimestamp = dateInfoToDate(this.birthDate)
            rv.roles = rolesToString(this.roles)
        } catch (e: Exception) {
            println("toEntity failed: ${e.message}")
            throw e
        }
        return rv
    }

    override fun toString(): String {
        return  "{" +
                "email:$email, " +
                "name:$name, " +
                "createdTimestamp:$birthDate" +
                "roles:$roles, " +
                "}"
    }


    fun isDateEmpty(date: DateInfo): Boolean {
        return date.day == 0 && date.month == 0 && date.year == 0
    }


    companion object {


        private fun dateToDateInfo(date: LocalDateTime?): DateInfo? {
            return date?.let {
                DateInfo(
                    day = it.dayOfMonth,
                    month = it.monthValue,
                    year = it.year
                )
            }
        }

        private fun dateInfoToDate(dateInfo: DateInfo?): LocalDateTime? {
            return dateInfo?.let {
                LocalDateTime.of(it.year, it.month, it.day, 0, 0)
            }
        }

        fun rolesToString(roles: List<String>?): String? {
            return roles?.joinToString(prefix = "[", postfix = "]", separator = ",")
        }

        private fun stringToRoles(rolesString: String?): List<String>? {
            if (rolesString.isNullOrBlank()) return null

            return rolesString
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }


    }

}