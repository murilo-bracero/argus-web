package br.com.argus.customer_backend.services

import br.com.argus.customer_backend.dto.CreateCustomerRequest
import br.com.argus.customer_backend.models.Customer
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomerService {

    fun save(request: CreateCustomerRequest) : Customer

    fun delete(id: ObjectId)

    fun findByCpf(cpf: String) : Customer

    fun findAll(pageable: Pageable) : Page<Customer>

    fun findById(id: String) : Customer

}