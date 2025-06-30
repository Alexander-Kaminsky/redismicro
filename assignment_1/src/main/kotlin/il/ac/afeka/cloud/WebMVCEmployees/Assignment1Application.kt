package il.ac.afeka.cloud.WebMVCEmployees


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class Assignment1Application

fun main(args: Array<String>) {
    SpringApplicationBuilder(Assignment1Application::class.java)
        .profiles("servicePostgres")
        .run(*args)
}
