package br.com.argus.customer_backend.services.impl

import br.com.argus.customer_backend.dto.CreateCredentialsRequest
import br.com.argus.customer_backend.dto.CreateCustomerRequest
import br.com.argus.customer_backend.exception.CustomerException
import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import br.com.argus.customer_backend.services.AuthenticationService
import br.com.argus.customer_backend.services.CustomerService
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.Stream
import kotlin.NoSuchElementException

@Service
class CustomerServiceImpl(
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val authenticationService: AuthenticationService
) : CustomerService {

    private val log = KotlinLogging.logger {}


    override fun save(request: CreateCustomerRequest): Customer {

        val saved = customerRepository.save(request.toModel())

        val req = CreateCredentialsRequest(saved.id.toHexString(), request.email, request.password)

        authenticationService.createCredentials(req)

        return saved
    }

    override fun delete(id: ObjectId) {
        val customer = customerRepository.findById(id).orElseThrow()

        try {
            authenticationService.deleteCredentials(customer.idpId)
        } catch (e: Exception){
            log.error { "Could not remove credentials of user ${id.toHexString()}. $e" }
            throw CustomerException("CS001", "An error while removing user credentials occurred", e)
        }

        customerRepository.deleteById(id)
    }

    override fun findByCpf(cpf: String): Customer {
        return customerRepository.findByCpf(cpf).orElseThrow()
    }

    override fun findAll(pageable: Pageable): Page<Customer> {
        return customerRepository.findAll(pageable)
    }

    override fun findById(id: String): Customer {
        val oid = ObjectId(id)

        return customerRepository.findById(oid).orElseThrow()
    }

}