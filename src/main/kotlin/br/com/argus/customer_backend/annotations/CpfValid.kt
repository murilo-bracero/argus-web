package br.com.argus.customer_backend.annotations

import br.com.argus.customer_backend.validators.CpfValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CpfValidator::class])
@MustBeDocumented
annotation class CpfValid (

    val message: String = "CPF is invalid",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []

)