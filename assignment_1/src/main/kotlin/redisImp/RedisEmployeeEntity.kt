package redisImp

import il.ac.afeka.cloud.WebMVCEmployees.DateInfo
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable

@RedisHash("Employee")
data class RedisEmployeeEntity(
    @Id val email: String,
    val name: String?,
    var password: String?,
    val birthTimestamp: DateInfo?,
    val roles: List<String> = listOf()
): Serializable
