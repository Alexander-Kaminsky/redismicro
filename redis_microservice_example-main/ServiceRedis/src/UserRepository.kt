package com.example.servicea.repository

import com.example.servicea.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
