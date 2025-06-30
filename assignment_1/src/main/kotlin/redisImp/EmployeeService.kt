package redisImp

import org.springframework.stereotype.Service

@Service
interface EmployeeService {
    fun createEmployee (employee:EmployeeBoundary):EmployeeBoundary
    fun getEmployee(email: String, password: String) : EmployeeBoundary
    fun getByDomain(domain:String,page: Int,size: Int) : List<EmployeeBoundary>
    fun getByRole(role:String,page:Int,size:Int) : List<EmployeeBoundary>
    fun getByAge(age:Int,page: Int,size: Int)  : List<EmployeeBoundary>
    fun getAll(page:Int,size:Int) : List<EmployeeBoundary>
    fun deleteAll()

}