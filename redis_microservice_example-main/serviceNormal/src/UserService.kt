package com.example.servicea.service

import com.example.servicea.model.User
import com.example.servicea.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUserById(id: Long): User? = userRepository.findById(id).orElse(null)
}
