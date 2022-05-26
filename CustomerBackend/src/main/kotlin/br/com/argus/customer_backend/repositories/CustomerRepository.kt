package br.com.argus.customer_backend.repositories

import br.com.argus.customer_backend.models.Customer
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository : MongoRepository<Customer, ObjectId> {

    fun findByCpf(cpf: String): Optional<Customer>

    fun findByEmail(email: String): Optional<Customer>

    fun findByNameLike(name: String, pageable: Pageable): Page<Customer>
}