# Redis Microservice Example
This repository contains a simple microservice examples built with Spring Boot, demonstrating interaction with Redis and PostgreSQL. It includes integration tests to verify functionality using both data stores.

# 🚀 Prerequisites
Docker must be installed and running.

# 🧪 How to Run PostgreSQL Test
Launch the application:

Run Assignment1Application.kt
Once the server has started successfully, execute:

Run PostgresEmployeeServiceTest
# 🧪 How to Run Redis Test
Launch the Redis application:

Run redisApplication.kt
Once the server is up, execute:

Run RedisEmployeeServiceTest
# 📁 Project Structure
Assignment1Application.kt – Main application entry point for PostgreSQL tests

redisApplication.kt – Main application entry point for Redis tests

PostgresEmployeeServiceTest – Integration test for PostgreSQL

RedisEmployeeServiceTest – Integration test for Redis

# 📝 Notes
Ensure the appropriate Docker container (Redis/Postgres) is running before executing the tests.

All configuration values are located in application.yml / application.properties.
