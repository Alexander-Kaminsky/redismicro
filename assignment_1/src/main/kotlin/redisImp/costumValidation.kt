package redisImp

import jakarta.validation.constraints.NotBlank

data class Role(
    @field:NotBlank(message = "Role must not be blank")
    val value: String){

}