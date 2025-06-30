package redisImp

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.net.Socket
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import il.ac.afeka.cloud.WebMVCEmployees.DateInfo
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

fun main() {
    // Test if Redis is reachable
    try {
        Socket("localhost", 6379).use {
            println("✅ Redis is reachable on port 6379")
        }
    } catch (e: Exception) {
        println("❌ Redis is NOT reachable: ${e.message}")
        return
    }

    val restTemplate = RestTemplate().apply {
        messageConverters.add(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
    }
    val httpHeaders = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }

    val postStart = System.currentTimeMillis()

    restTemplate.delete("http://localhost:8082/employees")

    for (i in 0 until 100) {
        val employee = EmployeeBoundary(
            email = "test$i@example.com",
            name = "John",
            password = "Password123",
            birthDate = DateInfo(1,1,1),
            roles = listOf("Admin")
        )

        val request = HttpEntity(employee, httpHeaders)
        try {
            restTemplate.postForEntity("http://localhost:8082/employees", request, String::class.java)
        } catch (e: Exception) {
            println("❌ POST failed for ${employee.email}: ${e.message}")
            println("the request:${request}")
            println("Serialized Request Body: ${jacksonObjectMapper().writeValueAsString(employee)}")
        }
    }

    val postEnd = System.currentTimeMillis()
    println("⏱️ HTTP POST insertions took ${postEnd - postStart} ms")

    // Repeated GET requests to measure caching effect
    val repeatedEmails = listOf(
        "test0@example.com", "test0@example.com", "test0@example.com",
        "test50@example.com", "test50@example.com",
        "test99@example.com", "test99@example.com",
        "test20@example.com", "test70@example.com",
        "test0@example.com", "test50@example.com"
    )

    val getRepeatedStart = System.currentTimeMillis()
    for (email in repeatedEmails) {
        try {
            val response = restTemplate.getForObject(
                "http://localhost:8082/employees/$email?password=Password123",
                String::class.java
            )
            println("📥 GET $email → ${response?.substring(0..minOf(50, response.length - 1))}...")
        } catch (e: Exception) {
            println("❌ GET failed for http://localhost:8082/employees/$email?password=Password123 ${e.message}")
        }
    }
    val getRepeatedEnd = System.currentTimeMillis()
    println("⏱️ Repeated GETs took ${getRepeatedEnd - getRepeatedStart} ms")

    // Final: GET all employees
    val getAllStart = System.currentTimeMillis()
    try {
        val allResponse = restTemplate.getForObject("http://localhost:8082/employees", String::class.java)
        println("✅ GET all employees, size: ${allResponse?.length} chars")
    } catch (e: Exception) {
        println("❌ GET ALL failed: ${e.message}")
    }
    val getAllEnd = System.currentTimeMillis()
    println("⏱️ GET ALL took ${getAllEnd - getAllStart} ms")
}
