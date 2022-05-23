package br.com.argus.authapi.dto

import br.com.argus.authapi.model.SystemEnum
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateUserCredentialsDTO (

    @field:NotBlank
    @field:Size(min = 24, max = 24)
    val userId: String = "",

    @field:NotNull
    val userSystem: SystemEnum = SystemEnum.UNKNOWN
        )