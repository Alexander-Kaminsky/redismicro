package il.ac.afeka.cloud.projmvc.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE) // Apply to the BirthdateBoundary class
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidDateValidator::class])
@MustBeDocumented
annotation class ValidDate(
    val message: String = "Invalid date components (day, month, year do not form a valid date)",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)