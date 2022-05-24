package br.com.argus.authapi.service.impl

import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.repository.UserCredentialRepository
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.service.EncryptionService
import org.apache.commons.codec.binary.Base32
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Service
import kotlin.NoSuchElementException

@Service
class CredentialsServiceImpl(
    @Autowired private val mongoTemplate: MongoTemplate,
    @Autowired private val userCredentialRepository: UserCredentialRepository,
    @Autowired private val encryptionService: EncryptionService
): CredentialsService {

    override fun createCredential(cred: UserCredential) {

        val exists = mongoTemplate.exists<User>(query(where("id").`is`(ObjectId(cred.userId))), cred.system.collection)

        if (!exists){
            throw NoSuchElementException()
        }

        userCredentialRepository.save(cred)

    }

    override fun remove(userEmail: String, userId: String) {
        TODO("Not yet implemented")
    }

    override fun find(userId: String, system: SystemEnum): UserCredential {
        val creds = userCredentialRepository.findByUserIdAndSystem(userId, system)
            .orElseThrow()

        creds.secret = encryptionService.decrypt(creds.secret)
        creds.refreshToken = encryptionService.decrypt(creds.refreshToken)
        return creds
    }

    override fun update(newCreds: UserCredential) : String? {
        var secret: String? = null

        // user has deactivated mfa
        if(!newCreds.mfaEnabled && newCreds.secret.isNotEmpty()){
            newCreds.secret = ""
        }

        // user has enabled mfa for the first time
        if(newCreds.mfaEnabled && newCreds.secret.isEmpty()){
            newCreds.secret = encryptionService.generateSecret()
            secret = Base32().encodeAsString(newCreds.secret.toByteArray(Charsets.UTF_8))
        }

        newCreds.secret = encryptionService.encrypt(newCreds.secret)
        newCreds.refreshToken = encryptionService.encrypt(newCreds.refreshToken)
        userCredentialRepository.save(newCreds)

        return secret
    }

}