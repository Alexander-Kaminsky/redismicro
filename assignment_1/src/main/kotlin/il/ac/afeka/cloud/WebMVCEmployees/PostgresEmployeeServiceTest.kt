package il.ac.afeka.cloud.WebMVCEmployees

import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.Socket

fun main() {

    // Test if PostgreSQL is reachable (via port check)
    try {
        Socket("localhost", 5432).use {
            println("✅ PostgreSQL is reachable on port 5432")
        }
    } catch (e: Exception) {
        println("❌ PostgreSQL is NOT reachable: ${e.message}")
        return
    }

    val restTemplate = RestTemplate()
    val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }
    val objectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
    }


    // Insert 100 employees
    val postStart = System.currentTimeMillis()
    restTemplate.delete("http://localhost:8081/employees")

    for (i in 0 until 100) {
        val employee = EmployeeBoundary(
            email = "test$i@example.com",
            name = "John",
            password = "Password123",
            birthDate = LocalDateTime.now().toDateInfo(),
            roles = listOf("Admin")
        )
        val body = objectMapper.writeValueAsString(employee)
        val entity = HttpEntity(body, headers)

        try {
            restTemplate.postForEntity("http://localhost:8081/employees", entity, String::class.java)
        } catch (e: Exception) {
            println("❌ POST failed for test$i@example.com: ${e.message}")
        }
    }
    val postEnd = System.currentTimeMillis()
    println("⏱️ PostgreSQL API POST (100 inserts) took ${postEnd - postStart} ms")

    // Repeated GET requests to simulate caching scenario
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
                "http://localhost:8081/employees/$email?password=Password123",
                String::class.java
            )
            println("📥 GET $email → ${response?.substring(0..minOf(50, response.length - 1))}...")
        } catch (e: Exception) {
            println("❌ GET failed for $email: ${e.message}")
        }
    }
    val getRepeatedEnd = System.currentTimeMillis()
    println("⏱️ Repeated GETs took ${getRepeatedEnd - getRepeatedStart} ms")

    // Final: GET all employees
    val getAllStart = System.currentTimeMillis()
    try {
        val allResponse = restTemplate.getForObject("http://localhost:8081/employees", String::class.java)
        println("✅ GET all employees, size: ${allResponse?.length} chars")
    } catch (e: Exception) {
        println("❌ GET ALL failed: ${e.message}")
    }
    val getAllEnd = System.currentTimeMillis()
    println("⏱️ GET ALL took ${getAllEnd - getAllStart} ms")
}
