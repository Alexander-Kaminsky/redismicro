package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidDateValidator::class])
annotation class ValidDate(
    val message: String = "Invalid date",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)