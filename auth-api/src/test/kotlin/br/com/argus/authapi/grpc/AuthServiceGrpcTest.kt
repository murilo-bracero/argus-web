package br.com.argus.authapi.grpc

import br.com.argus.authapi.AuthApiServiceGrpc
import br.com.argus.authapi.CreateCredentialsRequest
import br.com.argus.authapi.SYSTEM_ENUM
import br.com.argus.authapi.ValidateTokenRequest
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.utils.JwtUtils
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension::class)
class AuthServiceGrpcTest {


    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var credentialsService: CredentialsService

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    private lateinit var user: User
    private lateinit var authServiceGrpc:  AuthApiServiceGrpc.AuthApiServiceBlockingStub

    val grpcCleanup = GrpcCleanupRule()

    @BeforeEach
    fun setUp() {
        user = User(
            email = "generic.email@mail.com",
            hashPassword = "testhashpassword"
        )

        val serverName = InProcessServerBuilder.generateName()

        grpcCleanup.register(
            InProcessServerBuilder
                .forName(serverName).directExecutor().addService(AuthServiceGrpc(credentialsService, jwtUtils)).build()
                .start()
        )

        authServiceGrpc =
            AuthApiServiceGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
            )

        mongoTemplate.save(user, SystemEnum.CUSTOMER.collection)
    }

    @Test
    fun `should create new user credentials successfully`() {
        val req = CreateCredentialsRequest.newBuilder()
            .setUserId(user.id.toHexString())
            .setSystem(SYSTEM_ENUM.CUSTOMER)
            .build()


        authServiceGrpc.createCredentials(req)


        val creds = credentialsService.find(user.id.toHexString(), SystemEnum.CUSTOMER)

        assertNotNull(creds)
        assertEquals(user.id.toHexString(), creds.userId)
    }

    @Test
    fun `should throw error when trying to create credentials for a user that does not exists`() {
        val req = CreateCredentialsRequest.newBuilder()
            .setUserId(ObjectId.get().toHexString())
            .setSystem(SYSTEM_ENUM.CUSTOMER)
            .build()

        assertThrows<StatusRuntimeException> { authServiceGrpc.createCredentials(req) }
    }

    @Test
    fun `should return token credentials claims when it is valid`() {
        ifNotSave()
        val tokens = jwtUtils.generateToken(user.id.toHexString(), SystemEnum.CUSTOMER)

        val req = ValidateTokenRequest.newBuilder()
            .setToken(tokens.accessToken)
            .build()

        val credentials = authServiceGrpc.validateToken(req)

        assertEquals(user.id.toHexString(), credentials.userId)
    }

    @Test
    fun `should throw error when provided token is invalid`() {
        val req = ValidateTokenRequest.newBuilder()
            .setToken("mocked token")
            .build()

        assertThrows<StatusRuntimeException> { authServiceGrpc.validateToken(req) }
    }

    private fun ifNotSave(){
        try {
            credentialsService.find(user.id.toHexString(), SystemEnum.CUSTOMER)
        }catch (ex: NoSuchElementException){
            val creds = UserCredential(
                system = SystemEnum.CUSTOMER,
                userId = user.id.toHexString()
            )
            credentialsService.createCredential(creds)
        }
    }

}