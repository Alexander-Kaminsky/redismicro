package com.example.servicea.model

import jakarta.persistence.*
import io.swagger.v3.oas.annotations.media.Schema

@Entity
@Table(name = "users")
@Schema(description = "User entity")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Schema(description = "User's name")
    val name: String,

    @Schema(description = "User's email")
    val email: String
)
