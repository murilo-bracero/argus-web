package br.com.argus.customerbackend.annotations

import br.com.argus.customerbackend.validators.CpfValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CpfValidator::class])
@MustBeDocumented
annotation class CpfValid (

    val message: String = "CPF is invalid",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []

)