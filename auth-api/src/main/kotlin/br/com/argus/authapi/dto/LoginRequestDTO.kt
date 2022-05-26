package br.com.argus.authapi.dto

import br.com.argus.authapi.annotations.OriginSystemValid
import br.com.argus.authapi.model.SystemEnum
import javax.validation.constraints.Digits
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class LoginRequestDTO(

    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String,

    @field:OriginSystemValid
    val userSystem: SystemEnum = SystemEnum.UNKNOWN,

    val mfaCode: String?
) {
    constructor(email: String, password: String, userSystem: SystemEnum): this(email, password, userSystem, null)
}
