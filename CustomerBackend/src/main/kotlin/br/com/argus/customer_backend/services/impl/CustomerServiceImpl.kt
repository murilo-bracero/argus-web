package br.com.argus.customer_backend.services.impl

import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import br.com.argus.customer_backend.services.AuthenticationService
import br.com.argus.customer_backend.services.CustomerService
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException

@Service
class CustomerServiceImpl(
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val authenticationService: AuthenticationService
) : CustomerService {
    override fun save(customer: Customer): Customer {

        val saved = customerRepository.save(customer)

        authenticationService.createCredentials(saved.id.toHexString())

        return saved
    }

    override fun delete(id: ObjectId) {
        customerRepository.deleteById(id)
    }

    override fun findOne(id: ObjectId?, cpf: String?, email: String?): Customer {
       if(id != null){
           return customerRepository.findById(id).orElseThrow{ NoSuchElementException("id") }
       }

        if (!cpf.isNullOrEmpty()) {
            return customerRepository.findByCpf(cpf).orElseThrow{ NoSuchElementException("cpf") }
        }

        if(!email.isNullOrEmpty()) {
            return customerRepository.findByEmail(email).orElseThrow{ NoSuchElementException("email") }
        }

        throw NullPointerException()
    }

    override fun findMany(name: String?, pageable: Pageable) : Page<Customer> {
        if(name.isNullOrEmpty()) {
            return customerRepository.findAll(pageable)
        }

        return customerRepository.findByNameLike(name, pageable)
    }
}