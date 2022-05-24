package br.com.argus.authapi.service.impl

import br.com.argus.authapi.exception.AuthNeedsMfaException
import br.com.argus.authapi.model.AuthResultEnum
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.repository.UserCredentialRepository
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.service.UserAuthenticationService
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import org.apache.commons.codec.binary.Base32
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.*

@Service
class UserAuthenticationServiceImpl(
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val credentialsService: CredentialsService,
    @Autowired private val mongoTemplate: MongoTemplate
) : UserAuthenticationService {
    override fun login(email: String, rawPassword: String, code: String?, system: SystemEnum): User {
        val user =
            Optional.ofNullable(mongoTemplate.findOne<User>(query(where("email").`is`(email)), system.collection))
                .orElseThrow { throw BadCredentialsException("credentials are invalid") }

        if (!passwordEncoder.matches(rawPassword, user.hashPassword)) {
            throw BadCredentialsException("credentials are invalid")
        }

        val auth = credentialsService.find(user.id.toHexString(), system)

        user.credentials = auth

        if (!auth.mfaEnabled) {
            return user
        }

        if (code == null) {
            throw AuthNeedsMfaException()
        }

        val secret = Base32().encodeAsString(auth.secret.toByteArray(Charsets.UTF_8))

        if (!GoogleAuthenticator(secret.toByteArray(Charsets.UTF_8)).isValid(code)) {
            throw BadCredentialsException("invalid token")
        }
        return user
    }
}