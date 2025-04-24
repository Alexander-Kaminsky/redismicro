package il.ac.afeka.cloud.redis.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisEmployeeRepository : PagingAndSortingRepository<RedisEmployeeEntity, String> { // Extends Crud + Pagination/Sorting

    // --- Custom Query Methods leveraging @Indexed fields ---

    // Find by exact role (requires 'roles' field to be @Indexed)
    // Spring Data Redis generates the necessary SCAN or index query based on configuration
    fun findByRolesContains(role: String, pageable: Pageable): Page<RedisEmployeeEntity>

    // Find by manager's email (requires 'managerEmail' field to be @Indexed)
    fun findByManagerEmail(managerEmail: String, pageable: Pageable): Page<RedisEmployeeEntity>

    // Note: Complex filtering like byEmailDomain or byAge directly via repository methods
    // is generally NOT supported out-of-the-box by basic Spring Data Redis without
    // secondary indexes (like RediSearch) or custom implementations.
    // These filters will often be implemented in the service layer by fetching and filtering.
}