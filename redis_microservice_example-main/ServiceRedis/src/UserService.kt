package com.example.serviceb.service

import com.example.serviceb.model.User
import com.example.serviceb.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    @Cacheable("users", key = "#id")
    fun getUserById(id: Long): User? {
        println("Fetching from DB: $id")  // Debugging cache usage
        return userRepository.findById(id).orElse(null)
    }
}
