package il.ac.afeka.cloud.redis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
@EnableRedisRepositories // Essential for finding Redis repositories
class RedisApplication

fun main(args: Array<String>) {
    runApplication<RedisApplication>(*args)
}