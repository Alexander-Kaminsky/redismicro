package il.ac.afeka.cloud.WebMVCEmployees

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EmployeeServiceImpl(
    private val employeeCrud: EmployeeCrud,
) : EmployeeService {

    @org.springframework.transaction.annotation.Transactional(readOnly = false)
    override fun createEmployee(employee: EmployeeBoundary): EmployeeBoundary{
        /*
        פעולה שמקבלת פרטים של עובדת בחברה, שיש לאחסן בשירות.
       פעולה זו מחזירה את הפרטים, לאחר שמירתם, למפעיל השירות
       במידה וכבר קיימים פרטים עם הדואל שהועבר לשירות, הפעולה תכשל
        */

        if (this.employeeCrud.existsByEmail(employee.email!!))
            throw (InvalidInputException("info already exist"))

        if(!employee.password!!.trim().contains(Regex("[A-Z]+")) ||
            !employee.password!!.trim().contains(Regex("[0-9]+")))
            throw (InvalidInputException("password must contain at least one digit and one upper letter"))

        if(employee.birthDate == null ||
            employee.isDateEmpty(employee.birthDate!!))
            throw (InvalidInputException("birth Date must be of the following format {\"day\":\"**\", \"month\":\"**\", \"year\":\"****\"}"))

        if(employee.roles == null ||
            employee.roles!!.isEmpty() ||
           !employee.roles!!.all { s: String ->  s.trim().isNotEmpty()}
            )
            throw (InvalidInputException("roles must contain at least one entry"))

        return EmployeeBoundary(this.employeeCrud.save(employee.toEntity()))
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    override fun getEmployee(email: String, password: String) : EmployeeBoundary{
        /*
        פעולה שמחזירה פרטי עובדת ספציפית.
        פעולה זו מקבלת כפרמטרים את הדואל של העובדת ואת הסיסמא שלה, כפי שנשמרה בשירות.
        במידה וקיימים בשירות נתונים על עובדים עם ה-email והסיסמא שהועברו, הפעולה תחזיר למפעיל השירות את הפרטים.
        במידה והנתונים לא קיימים בשירות, בדיוק כפי שהועברו בפרמטרים, הפעולה תחזיר שגיאה מתאימה
        שימו לב כי פעולה זו לא חושפת סיסמאות, אלא מחזירה את כל הפרטים השמורים בשירות, פרט לסיסמא
         */
        val employeeE = this.employeeCrud.findByEmail(email)
            .orElseThrow { EmployeeNotFoundException("Employee with given credentials not found") }

        if (password != employeeE.passwordHash){
            throw(EmployeeNotFoundException("Employee with given credential not found"))
        }
        val employeeB = EmployeeBoundary(employeeE)
        employeeB.password = null
        return employeeB
    }

    override fun getAll(page:Int,size:Int) : List<EmployeeBoundary>{
        /*
        פעולה שמחזירה את פרטי העובדים שנשמרו בשירות, ושתומכת ב-pagination
        שימו לב כי גם פעולה זו לא חושפת את הסיסמאות של העובדים
         */
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))
        return employeeCrud.findAll(pageable)
            .stream()
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun getByDomain(email:String ,page: Int ,size: Int) : List<EmployeeBoundary>{
        /*
        פעולה שמחזירה את פרטי העובדים, שה-domain ב-email שלהם מתאים בדיוק לקלט שהפעולה מקבלת במשתנה domain.
        פעולה זו תומכת ב-pagination. שימו לב כי גם פעולה זו לא חושפת סיסמאות
        אם לא קיימים בשירות עובדים עם ה-domain המבוקש, הפעולה תחזיר מערך ריק
         */
        val domain = if (email.matches(regex = Regex("^.*@.+$"))){
            getDomain(email)
        }else{
            email
        }
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))

        val employeesOfDomain = employeeCrud.findByEmailDomain(domain,pageable)

        return employeesOfDomain
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun getByRole(role:String,page:Int,size:Int) : List<EmployeeBoundary>{
        /*
        פעולה שמחזירה את פרטי העובדים, שאחד התפקידים שלהם, מתאים בדיוק למשתנה role.
        פעולה זו תומכת ב-pagination. שימו לב כי גם פעולה זו לא חושפת סיסמאות.
        אם לא קיימים בשירות עובדים עם התפקיד המבוקש, הפעולה תחזיר מערך ריק
         */
        if(role.trim().isEmpty())
            return emptyList()

        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))

        return employeeCrud.findAllByRolesContains(EmployeeBoundary.rolesToString(listOf(role.trim()))!!,pageable)
            .stream()
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun getByAge(age:Int,page: Int,size: Int) : List<EmployeeBoundary>{
        /*פעולה שמחזירה את פרטי העובדים, שהגיל שלהם בשנים, הועבר כפרמטר ageInYears.
        למשל, אם פעולה זו הופעלה, כדי לחפש עובדים בני 30 ב-1 באפריל 2025, היא תחזיר את כל העובדים שיום הולדתם ה-30 חל בין 1 באפריל 2024 ל-1 באפריל 2025
        פעולה זו תומכת ב-pagination. שימו לב כי גם פעולה זו לא חושפת סיסמאות.
        אם לא קיימים בשירות עובדים בגיל המבוקש, הפעולה תחזיר מערך ריק*/
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))

        val currentDateTime: LocalDateTime = LocalDateTime.now()
        val minForAge = currentDateTime.minusYears((age+1).toLong())
        val maxForAge = minForAge.plusYears(1).minusDays(1)

        return employeeCrud
            .findAllByBirthTimestampBetween(minForAge,maxForAge,pageable)
            .stream()
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun deleteAll() {
        /*
        פעולה שתעזור לך לבדוק את השרות, שמוחקת את כל המידע בשירות, כולל קשרים בין נתונים, אם הגדרת כאלה
         */
        return employeeCrud.deleteAll()
    }

    //////////////////////////
    //  utils
    /////////////////////////
    fun getDomain(email: String): String{
        if(!email.matches(Regex("^[A-Za-z0-9]+@.+$")))
            throw(InvalidEmailException("invalid email format"))
        val domain = email.split("@")[1]
        return domain
    }

}