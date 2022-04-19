package br.com.argus.customer_backend.controllers

import br.com.argus.customer_backend.dto.CustomerRequestDTO
import br.com.argus.customer_backend.dto.CustomerResponseDTO
import br.com.argus.customer_backend.dto.ErrorResponseDTO
import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import com.mongodb.DuplicateKeyException
import com.mongodb.MongoWriteException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/customers")
class CustomerController(
  @Autowired private val passwordEncoder: PasswordEncoder,
  @Autowired private val customerRepository: CustomerRepository
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createCustomer(@Valid @RequestBody request: CustomerRequestDTO): CustomerResponseDTO {
    val customer = Customer.from(request, passwordEncoder)

    return customerRepository.save(customer).to()
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(value = [(MongoWriteException::class)])
  fun handleException(): ErrorResponseDTO {
    return ErrorResponseDTO("002", "customer already exists")
  }

}