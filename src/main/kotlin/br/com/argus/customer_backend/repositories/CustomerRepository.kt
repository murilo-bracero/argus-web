package br.com.argus.customer_backend.repositories

import br.com.argus.customer_backend.models.Customer
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : MongoRepository<Customer, ObjectId> {
}