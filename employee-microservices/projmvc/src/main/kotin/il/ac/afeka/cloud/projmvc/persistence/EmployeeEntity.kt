package il.ac.afeka.cloud.projmvc.persistence

import il.ac.afeka.cloud.projmvc.data.BirthdateBoundary
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "EMPLOYEES")
class EmployeeEntity(
    @Id
    @Column(nullable = false, unique = true)
    var email: String, // Use var if JPA requires setters

    @Column(nullable = false)
    var name: String,

    // WARNING: Storing plain text password - Highly insecure! Hash in production.
    @Column(nullable = false)
    var password: String,

    // Store BirthdateBoundary fields directly or convert to LocalDate
    // Option 1: Store as LocalDate (Recommended for date operations)
    @Column(nullable = false)
    var birthdate: LocalDate,

    // Option 2: Embed BirthdateBoundary (Requires BirthdateBoundary marked @Embeddable)
    // @Embedded
    // @AttributeOverrides(
    //     AttributeOverride(name = "day", column = Column(name = "birth_day")),
    //     AttributeOverride(name = "month", column = Column(name = "birth_month")),
    //     AttributeOverride(name = "year", column = Column(name = "birth_year"))
    // )
    // var birthdateBoundary: BirthdateBoundary,


    @ElementCollection(fetch = FetchType.EAGER) // Store roles in a separate table
    @CollectionTable(name = "EMPLOYEE_ROLES", joinColumns = [JoinColumn(name = "employee_email")])
    @Column(name = "role", nullable = false)
    var roles: MutableSet<String> = mutableSetOf(), // Use MutableSet for JPA

    // --- Bonus Fields ---
    @ManyToOne(fetch = FetchType.LAZY) // Many employees can have one manager
    @JoinColumn(name = "manager_email") // Foreign key column in EMPLOYEES table
    var manager: EmployeeEntity? = null

    // Subordinates are implicitly defined by the manager relationship, query for them.
    // @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    // var subordinates: MutableSet<EmployeeEntity> = mutableSetOf() // Can be added but often queried instead

) {
    // Default constructor required by JPA
    constructor() : this("", "", "", LocalDate.now(), mutableSetOf(), null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EmployeeEntity
        return email == other.email
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }
}