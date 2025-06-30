package redisImp

fun RedisEmployeeEntity.toBoundary(): EmployeeBoundary =
    EmployeeBoundary(this)
