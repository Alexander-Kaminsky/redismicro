package il.ac.afeka.cloud.WebMVCEmployees

import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.*

interface EmployeeCrud : JpaRepository<EmployeeEntity, Long>{

    fun save(
        @Param("employee") employee: EmployeeEntity
    ):EmployeeEntity

    fun findByEmail(
        @Param("email") email: String): Optional<EmployeeEntity>

    @Query("SELECT e FROM EmployeeEntity e WHERE e.email LIKE CONCAT('%', :domain)")
    fun findByEmailDomain(
        @Param("domain") domain: String,
        pageable: Pageable): List<EmployeeEntity>

    /*
    fun findAll(
        @Param("Pageable") pageable: Pageable,
    ): List<EmployeeEntity>
     */

    fun findAllByRolesContains(
        @Param("roles") roles: String,
        pageable: Pageable,
    ): List<EmployeeEntity>

    fun findAllByBirthTimestampBetween(
        @Param("minDate") minDate: LocalDateTime,
        @Param("maxDate") maxDate: LocalDateTime,
        pageable: Pageable,
    ): List<EmployeeEntity>

    fun existsByEmail(
        @Param("email") email: String):Boolean


    //override fun deleteAll()


    fun findAllByParent_Id(
        @Param("parent") parentId: Long,
        pageable: Pageable):Page<EmployeeEntity>

}