package br.com.argus.customerbackend.controllers

import br.com.argus.customerbackend.dto.CreateCustomerRequest
import br.com.argus.customerbackend.dto.CustomerResponse
import br.com.argus.customerbackend.mapper.CustomerMapper
import br.com.argus.customerbackend.services.CustomerService
import br.com.argus.customerbackend.utils.isCpfValid
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort.Direction
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/customers")
class CustomerController(
    @Autowired private val customerService: CustomerService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@Valid @RequestBody request: CreateCustomerRequest): CustomerResponse {
        val customer = customerService.save(request)

        return CustomerMapper.toDto(customer)
    }

    @GetMapping("/{id}")
    fun getCustomerById(@PathVariable id: String): CustomerResponse {
        val customer = customerService.findById(id)

        return CustomerMapper.toDto(customer)
    }

    @GetMapping(params = ["cpf"])
    fun getCustomerByCpf(@RequestParam cpf: String): CustomerResponse {
        if (!isCpfValid(cpf)) {
            throw InvalidPropertiesFormatException("cpf is invalid or malformed")
        }

        val customer = customerService.findByCpf(cpf)

        return CustomerMapper.toDto(customer)
    }

    @GetMapping
    fun getAllCustomers(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "ASC") direction: String) : Page<CustomerResponse> {

        val pageRequest = PageRequest.of(page, size, Direction.valueOf(direction), "id")

        val pageCustomer = customerService.findAll(pageRequest)

        return pageCustomer.map { CustomerMapper.toDto(it) }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeCustomer(@PathVariable id: String){
        customerService.delete(ObjectId(id))
    }
}