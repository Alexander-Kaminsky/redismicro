package redisImp


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class redisApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder(redisApplication::class.java)
        .profiles("serviceRedis")
        .run(*args)
}