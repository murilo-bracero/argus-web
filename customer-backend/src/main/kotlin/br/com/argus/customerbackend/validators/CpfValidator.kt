package br.com.argus.customerbackend.validators

import br.com.argus.customerbackend.annotations.CpfValid
import br.com.argus.customerbackend.utils.isCpfValid
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CpfValidator: ConstraintValidator<CpfValid, String> {
    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return isCpfValid(value)
    }
}