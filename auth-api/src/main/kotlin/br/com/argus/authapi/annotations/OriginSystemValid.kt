package br.com.argus.authapi.annotations

import br.com.argus.authapi.model.SystemEnum
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OriginSystemValidator::class])
@MustBeDocumented
annotation class OriginSystemValid(
    val message: String = "User's origin system is invalid",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

class OriginSystemValidator: ConstraintValidator<OriginSystemValid, SystemEnum> {
    override fun isValid(value: SystemEnum?, context: ConstraintValidatorContext?): Boolean {
        return value != null && value != SystemEnum.UNKNOWN
    }
}