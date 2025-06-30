package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "employees")
class EmployeeEntity (
    @Id @GeneratedValue
    var id: Long?,

    var name:String?,
    var email:String?,

    //@Column(nullable = false)
    var passwordHash: String?,
    @Temporal(TemporalType.TIMESTAMP)
    var birthTimestamp: LocalDateTime?,
    var roles: String?,
    @ManyToOne(targetEntity = EmployeeEntity::class, fetch = FetchType.EAGER, optional = true) var parent: EmployeeEntity?){

    constructor():this(null,null,null,null,null,null,null)

    override fun toString(): String {
        return "{" +
                "email:$email, " +
                "name:$name, " +
                "birthdate:$birthTimestamp" +
                "roles:$roles" +
                "}"
    }

}