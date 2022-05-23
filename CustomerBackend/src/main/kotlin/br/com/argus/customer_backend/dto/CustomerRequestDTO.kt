package br.com.argus.customer_backend.dto

import br.com.argus.customer_backend.annotations.CpfValid
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CustomerRequestDTO (
  @field:NotBlank
  @field:Size(min = 11, max = 11)
  @field:Digits(integer = 11, fraction = 0)
  @field:CpfValid
  val cpf: String = "",

  @field:NotBlank
  val name: String = "",

  @field:NotBlank
  val email: String = "",

  @field:NotBlank
  @field:Size(min = 10)
  val password: String = "",

  val profilePicUri: String = "",
    )