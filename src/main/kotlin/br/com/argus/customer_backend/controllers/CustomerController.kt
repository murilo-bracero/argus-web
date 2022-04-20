package br.com.argus.customer_backend.controllers

import br.com.argus.customer_backend.annotations.CpfValid
import br.com.argus.customer_backend.dto.CustomerRequestDTO
import br.com.argus.customer_backend.dto.CustomerResponseDTO
import br.com.argus.customer_backend.dto.ErrorResponseDTO
import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import br.com.argus.customer_backend.utils.isCpfValid
import com.mongodb.MongoWriteException
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Size
import kotlin.NoSuchElementException

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

  @GetMapping("/{id}")
  fun getCustomerById(@PathVariable id: String): CustomerResponseDTO {
    val customer = customerRepository.findById(ObjectId(id))
      .orElseThrow()

    return customer.to()
  }

  @GetMapping(params = ["cpf"])
  fun getCustomerByCpf(@RequestParam cpf: String): CustomerResponseDTO {
    if(!isCpfValid(cpf)){
      throw InvalidPropertiesFormatException("cpf is invalid or malformed")
    }

    val customer = customerRepository.findByCpf(cpf)
      .orElseThrow()

    return customer.to()
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = [(InvalidPropertiesFormatException::class)])
  fun handleInvalidPropertiesFormatException(ex: InvalidPropertiesFormatException): ErrorResponseDTO {
    return ErrorResponseDTO("001", ex.message.orEmpty())
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = [(NoSuchElementException::class)])
  fun handleNoSuchElementException(): ErrorResponseDTO {
    return ErrorResponseDTO("003", "customer with provided id does not exists")
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(value = [(MongoWriteException::class)])
  fun handleException(): ErrorResponseDTO {
    return ErrorResponseDTO("002", "customer already exists")
  }

}