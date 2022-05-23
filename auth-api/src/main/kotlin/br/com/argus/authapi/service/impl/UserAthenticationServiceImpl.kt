package br.com.argus.authapi.service.impl

import br.com.argus.authapi.model.AuthResultEnum
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.repository.UserCredentialRepository
import br.com.argus.authapi.service.UserAthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserAthenticationServiceImpl(
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val userCredentialRepository: UserCredentialRepository,
    @Autowired private val mongoTemplate: MongoTemplate
): UserAthenticationService {
    override fun login(email: String, rawPassword: String, code: String?, system: SystemEnum): AuthResultEnum {
        val user = mongoTemplate.findOne<User>(query(where("email").`is`(email)), system.collection)
            ?: return AuthResultEnum.LOGIN_DENIED

        if (!passwordEncoder.matches(rawPassword, user.hashPassword)) {
            return AuthResultEnum.LOGIN_DENIED
        }

        val auth = userCredentialRepository.findByUserIdAndSystem(user.id.toHexString(), system)
            .orElseThrow()

        if(auth.mfaEnabled && code == null){
            return AuthResultEnum.NEED_MFA
        }

        //TODO:mfa verification

        return AuthResultEnum.AUTHENTICATED
    }

    override fun findByToken(token: String): Optional<User> {
        TODO()
    }
}