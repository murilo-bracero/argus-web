package br.com.argus.authapi.service.impl

import br.com.argus.authapi.exception.AuthNeedsMfaException
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.model.UserCredential
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
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.security.auth.login.AccountException
import javax.security.auth.login.AccountLockedException
import org.springframework.security.authentication.AccountExpiredException

@Service
class UserAuthenticationServiceImpl(
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val credentialsService: CredentialsService,
    @Autowired private val mongoTemplate: MongoTemplate
) : UserAuthenticationService {
    override fun login(email: String, rawPassword: String, code: String?, system: SystemEnum): User {
        val user = getUser(email, system)

        if (!passwordEncoder.matches(rawPassword, user.hashPassword)) {
            throw BadCredentialsException("credentials are invalid")
        }

        user.credentials = credentialsService.find(user.id.toHexString(), system)

        verifyAccount(user.credentials)

        if (user.credentials.mfaEnabled) {
            verifyMfa(user.credentials, code)
        }

        return user
    }

    private fun verifyAccount(creds: UserCredential) {
        if (creds.accountLocked){
            throw AccountLockedException()
        }

        if (creds.accountExpired){
            throw AccountExpiredException("user account is expired")
        }

        if (creds.credentialsExpired){
            throw CredentialsExpiredException("user credentials are expired")
        }

        if (!creds.isEnabled){
            throw AccountException("account is disabled")
        }
    }

    private fun verifyMfa(creds: UserCredential, code: String?){
        if(code == null) {
            throw AuthNeedsMfaException()
        }

        val secret = Base32().encodeAsString(creds.secret.toByteArray(Charsets.UTF_8))

        if (!GoogleAuthenticator(secret.toByteArray(Charsets.UTF_8)).isValid(code)) {
            throw BadCredentialsException("invalid token")
        }
    }

    private fun getUser(email: String, system: SystemEnum): User{
        return Optional.ofNullable(mongoTemplate.findOne<User>(query(where("email").`is`(email)), system.collection))
            .orElseThrow { throw BadCredentialsException("credentials are invalid") }
    }
}