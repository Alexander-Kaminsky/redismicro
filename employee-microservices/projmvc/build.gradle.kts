// build.gradle.kts (in root: employee-microservices/)
plugins {
    kotlin("jvm") version "1.9.25" apply false // Apply plugins in subprojects
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.2.5" apply false // Use a consistent Spring Boot version
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
}

allprojects {
    group = "il.ac.afeka.cloud"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        // sourceCompatibility = JavaVersion.VERSION_21 // Set in toolchain
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencies {
        // Common Dependencies for both modules
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0") // Compatible with Spring Boot 3.2.x

        developmentOnly("org.springframework.boot:spring-boot-devtools")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Specific configurations for projmvc
project(":projmvc") {
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa") // Apply JPA plugin only here

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")
        runtimeOnly("org.postgresql:postgresql")
    }

    // Configure Kotlin JPA plugin specifically for this module
    allOpen {
        annotation("jakarta.persistence.Entity")
        annotation("jakarta.persistence.MappedSuperclass")
        annotation("jakarta.persistence.Embeddable")
    }
}

// Specific configurations for redis
project(":redis") {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-redis")
        implementation("org.springframework.boot:spring-boot-starter-web") // Also needs web for controller
        // implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive") // Use if needed
        // implementation("io.projectreactor.kotlin:reactor-kotlin-extensions") // Use if needed
        // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor") // Use if needed
    }
}

// Ensure Spring plugin works correctly with Kotlin
spring {
    // configuration specific to the Spring plugin if needed
}