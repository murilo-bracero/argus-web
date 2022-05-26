package br.com.argus.customer_backend.services

import br.com.argus.customer_backend.models.Customer
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomerService {

    fun save(customer: Customer) : Customer

    fun delete(id: ObjectId)

    fun findOne(id: ObjectId? = null, cpf: String? = "", email: String? = ""): Customer

    fun findMany(name: String? = "", pageable: Pageable) : Page<Customer>

}