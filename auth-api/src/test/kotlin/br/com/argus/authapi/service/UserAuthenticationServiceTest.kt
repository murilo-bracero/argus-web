package br.com.argus.authapi.service

import br.com.argus.authapi.exception.AuthNeedsMfaException
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.service.impl.UserAuthenticationServiceImpl
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import org.apache.commons.codec.binary.Base32
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.security.auth.login.AccountException
import javax.security.auth.login.AccountLockedException

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension::class)
class UserAuthenticationServiceTest {

    @Autowired
    private lateinit var userAuthenticationServiceImpl: UserAuthenticationServiceImpl

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var credentialsService: CredentialsService

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private lateinit var storedUserWithMfa: User
    private lateinit var storedUserWithoutMfa: User
    private lateinit var storedUserExpired: User

    @BeforeEach
    fun setUp() {

        storedUserWithMfa = generateUser()
        storedUserWithoutMfa = generateUser()
        storedUserExpired = generateUser()

        if(isAlreadySaved(storedUserWithMfa.email) || isAlreadySaved(storedUserWithMfa.email) || isAlreadySaved(storedUserExpired.email)){
            return
        }

        storedUserExpired.credentials.accountExpired = true
        mongoTemplate.save(storedUserExpired, SystemEnum.CUSTOMER.collection)
        credentialsService.createCredential(storedUserExpired.credentials)

        mongoTemplate.save(storedUserWithMfa, SystemEnum.CUSTOMER.collection)
        credentialsService.createCredential(storedUserWithMfa.credentials)
        storedUserWithMfa.credentials.mfaEnabled = true
        credentialsService.update(storedUserWithMfa.credentials)

        mongoTemplate.save(storedUserWithoutMfa, SystemEnum.CUSTOMER.collection)
        credentialsService.createCredential(storedUserWithoutMfa.credentials)
    }

    @Test
    fun `should login successfully and return user when credentials provided are correct with no MFA`() {

        val user =
            userAuthenticationServiceImpl.login(storedUserWithoutMfa.email, "genericpassword", null, SystemEnum.CUSTOMER)

        assertNotNull(user)
        assertEquals(storedUserWithoutMfa.email, user.email)
    }

    @Test
    fun `should throw BadCredentialsException when credentials provided does not match with the ones stored in database`() {
        assertThrows<BadCredentialsException> {
            userAuthenticationServiceImpl.login(storedUserWithoutMfa.email, "genericpassword1", null, SystemEnum.CUSTOMER)
        }
    }

    @Test
    fun `should throw AuthNeedsMfaException when credentials are provided correctly but user needs mfa`() {

        assertThrows<AuthNeedsMfaException> {
            userAuthenticationServiceImpl.login(storedUserWithMfa.email, "genericpassword", null, SystemEnum.CUSTOMER)
        }
    }

    @Test
    fun `should login successfully when credentials and mfa are provided correctly`() {
        val creds = credentialsService.find(storedUserWithMfa.id.toHexString(), SystemEnum.CUSTOMER)

        val ga = GoogleAuthenticator(Base32().encodeAsString(creds.secret.toByteArray(Charsets.UTF_8)).toByteArray(Charsets.UTF_8))

        userAuthenticationServiceImpl.login(
            storedUserWithMfa.email,
            "genericpassword",
            ga.generate(),
            SystemEnum.CUSTOMER
        )

    }

    @Test
    fun `should throw BadCredentialsException when credentials are provided correctly but user mfa token not`() {
        assertThrows<BadCredentialsException> {
            userAuthenticationServiceImpl.login(
                storedUserWithMfa.email,
                "genericpassword",
                "15426535",
                SystemEnum.CUSTOMER
            )
        }
    }

    @Test
    fun `should throw AccountExpiredException when credentials are provided correctly but account is expired`(){
        assertThrows<AccountExpiredException> {
            userAuthenticationServiceImpl.login(
                storedUserExpired.email,
                "genericpassword",
                null,
                SystemEnum.CUSTOMER
            )
        }
    }

    @Test
    fun `should throw CredentialsExpiredException when credentials are provided correctly but account is expired`(){
        storedUserExpired.credentials.accountExpired = false
        storedUserExpired.credentials.credentialsExpired = true
        credentialsService.update(storedUserExpired.credentials)

        assertThrows<CredentialsExpiredException> {
            userAuthenticationServiceImpl.login(
                storedUserExpired.email,
                "genericpassword",
                null,
                SystemEnum.CUSTOMER
            )
        }
    }

    @Test
    fun `should throw AccountLockedException when credentials are provided correctly but account is locked`(){
        storedUserExpired.credentials.accountExpired = false
        storedUserExpired.credentials.credentialsExpired = false
        storedUserExpired.credentials.accountLocked = true
        credentialsService.update(storedUserExpired.credentials)

        assertThrows<AccountLockedException> {
            userAuthenticationServiceImpl.login(
                storedUserExpired.email,
                "genericpassword",
                null,
                SystemEnum.CUSTOMER
            )
        }
    }

    @Test
    fun `should throw AccountException when credentials are provided correctly but account is not enabled`(){
        storedUserExpired.credentials.accountExpired = false
        storedUserExpired.credentials.credentialsExpired = false
        storedUserExpired.credentials.accountLocked = false
        storedUserExpired.credentials.isEnabled = false
        credentialsService.update(storedUserExpired.credentials)

        assertThrows<AccountException> {
            userAuthenticationServiceImpl.login(
                storedUserExpired.email,
                "genericpassword",
                null,
                SystemEnum.CUSTOMER
            )
        }
    }

    fun isAlreadySaved(email: String): Boolean {
        return mongoTemplate.exists<User>(
            Query.query(Criteria.where("email").`is`(email)),
            SystemEnum.CUSTOMER.collection
        )
    }

    fun generateUser(): User {
        val id = ObjectId.get()

        val user = User(
            id,
            "${id.toHexString()}@mail.com",
            passwordEncoder.encode("genericpassword")
        )

        val creds = UserCredential(
            ObjectId.get(),
            id.toHexString(),
            "",
            false,
            SystemEnum.CUSTOMER
        )

        user.credentials = creds

        return user
    }

}