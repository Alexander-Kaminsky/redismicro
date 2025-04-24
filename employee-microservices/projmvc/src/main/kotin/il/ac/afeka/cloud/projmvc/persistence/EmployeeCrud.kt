package il.ac.afeka.cloud.projmvc.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface EmployeeCrud : JpaRepository<EmployeeEntity, String> { // String is the type of the Id (@Id var email: String)

    // Find by exact role (checks if the roles collection contains the given role)
    fun findByRolesContains(role: String, pageable: Pageable): Page<EmployeeEntity>

    // Find by manager's email (using the manager relationship)
    fun findByManagerEmail(managerEmail: String, pageable: Pageable): Page<EmployeeEntity>

    // Find by email domain (using database functions if possible, or LIKE)
    // Note: Adjust based on PostgreSQL capabilities
    @Query("SELECT e FROM EmployeeEntity e WHERE substring(e.email from position('@' in e.email) + 1) = :domain")
    fun findByEmailDomain(@Param("domain") domain: String, pageable: Pageable): Page<EmployeeEntity>

    // Find by age (calculating based on birthdate range)
    // This query finds employees whose birthday falls within the year preceding the target date
    @Query("SELECT e FROM EmployeeEntity e WHERE e.birthdate BETWEEN :startDate AND :endDate")
    fun findByBirthdateBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<EmployeeEntity>

}