package br.com.argus.authapi.service.impl

import br.com.argus.authapi.exception.AuthNeedsMfaException
import br.com.argus.authapi.model.AuthResultEnum
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.repository.UserCredentialRepository
import br.com.argus.authapi.service.UserAuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserAuthenticationServiceImpl(
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val userCredentialRepository: UserCredentialRepository,
    @Autowired private val mongoTemplate: MongoTemplate
) : UserAuthenticationService {
    override fun login(email: String, rawPassword: String, code: String?, system: SystemEnum): User {
        val user =
            Optional.ofNullable(mongoTemplate.findOne<User>(query(where("email").`is`(email)), system.collection))
                .orElseThrow { throw BadCredentialsException("credentials are invalid") }

        if (!passwordEncoder.matches(rawPassword, user.hashPassword)) {
            throw BadCredentialsException("credentials are invalid")
        }

        val auth = userCredentialRepository.findByUserIdAndSystem(user.id.toHexString(), system)
            .orElseThrow()

        if (auth.mfaEnabled && code == null) {
            throw AuthNeedsMfaException()
        }

        //TODO:mfa verification

        user.credentials = auth

        return user
    }
}