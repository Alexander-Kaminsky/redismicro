package il.ac.afeka.cloud.projmvc.logic

import il.ac.afeka.cloud.projmvc.data.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EmployeeService {
    fun createEmployee(employeeBoundary: EmployeeBoundary): EmployeeResponseBoundary
    fun getEmployeeByEmailAndPassword(email: String, passwordAttempt: String): EmployeeResponseBoundary
    fun getAllEmployees(pageable: Pageable): Page<EmployeeResponseBoundary>
    fun getEmployeesByCriteria(criteria: String, value: String, pageable: Pageable): Page<EmployeeResponseBoundary>
    fun deleteAllEmployees()

    // --- Bonus Methods ---
    fun assignManager(employeeEmail: String, managerEmailBoundary: ManagerEmailBoundary)
    fun getManager(employeeEmail: String): EmployeeResponseBoundary
    fun getSubordinates(managerEmail: String, pageable: Pageable): Page<EmployeeResponseBoundary>
    fun removeManager(employeeEmail: String)
}