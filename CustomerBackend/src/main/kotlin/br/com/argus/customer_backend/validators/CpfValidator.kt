package br.com.argus.customer_backend.validators

import br.com.argus.customer_backend.annotations.CpfValid
import br.com.argus.customer_backend.utils.isCpfValid
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CpfValidator: ConstraintValidator<CpfValid, String> {
    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return isCpfValid(value)
    }
}