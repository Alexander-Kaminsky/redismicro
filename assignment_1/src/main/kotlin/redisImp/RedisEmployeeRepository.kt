package redisImp

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisEmployeeRepository : CrudRepository<RedisEmployeeEntity, String>

