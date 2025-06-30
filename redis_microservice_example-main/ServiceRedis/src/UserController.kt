package com.example.servicea.controller

import com.example.servicea.model.User
import com.example.servicea.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "Operations related to Users")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    fun getUser(@PathVariable id: Long): User? = userService.getUserById(id)
}
