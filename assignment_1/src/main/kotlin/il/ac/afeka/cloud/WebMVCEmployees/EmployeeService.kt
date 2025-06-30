package il.ac.afeka.cloud.WebMVCEmployees

interface EmployeeService {
    fun createEmployee (employee:EmployeeBoundary):EmployeeBoundary
    fun getEmployee(email: String, password: String) : EmployeeBoundary
    fun getByDomain(email:String,page: Int,size: Int) : List<EmployeeBoundary>
    fun getByRole(role:String,page:Int,size:Int) : List<EmployeeBoundary>
    fun getByAge(age:Int,page: Int,size: Int)  : List<EmployeeBoundary>
    fun getAll(page:Int,size:Int) : List<EmployeeBoundary>
    fun deleteAll()
/*
    fun bind(id1:String, id2:String)
    fun getRelated(id:String, size:Int, page:Int): List<EmployeeBoundary>
    fun getParent(id:String): Optional<EmployeeBoundary>
 */


}
