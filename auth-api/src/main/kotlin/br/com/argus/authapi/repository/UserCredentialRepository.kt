package br.com.argus.authapi.repository

import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.model.UserCredential
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserCredentialRepository: MongoRepository<UserCredential, ObjectId>{

    fun findByUserIdAndSystem(userId: String, system: SystemEnum): Optional<UserCredential>

}