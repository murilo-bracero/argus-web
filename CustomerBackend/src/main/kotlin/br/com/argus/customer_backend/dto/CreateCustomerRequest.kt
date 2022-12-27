package br.com.argus.customer_backend.dto

import br.com.argus.customer_backend.annotations.CpfValid
import br.com.argus.customer_backend.models.Address
import br.com.argus.customer_backend.models.Customer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateCustomerRequest (
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

  val phoneNumber: String = "",

  @field:NotBlank
  val publicPlace: String = "",

  @field:NotNull
  val number: Int = 0,

  @field:NotBlank
  @field:Size(min = 8, max = 8)
  val zipCode: String = "",

  val complement: String = ""
) {
  fun toModel(): Customer {
    val address = Address(publicPlace, number, zipCode, complement)
    return Customer(cpf = cpf, name = name, profilePicUri = profilePicUri, address = address, email = email)
  }
}