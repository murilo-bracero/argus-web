package br.com.argus.customer_backend.services.impl

import br.com.argus.authapi.*
import br.com.argus.customer_backend.repositories.CustomerRepository
import br.com.argus.customer_backend.services.AuthenticationService
import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl(
    @Value("\${retry.max-retry}") private val maxRetry: Int,
    @Autowired private val authApiStub: AuthApiServiceGrpc.AuthApiServiceBlockingStub,
    @Autowired private val authApiAsyncStub: AuthApiServiceGrpc.AuthApiServiceStub,
    @Autowired private val customerRepository: CustomerRepository
) : AuthenticationService {

    private val log = KotlinLogging.logger {}

    override fun authenticate(token: String): Credentials {
        val req = ValidateTokenRequest.newBuilder().setToken(token).build();

        return authApiStub.validateToken(req)
    }

    override fun createCredentials(userId: String) {
        createCredentialsProxy(userId, 0)
    }

    private fun createCredentialsProxy(userId: String, retry: Int){
        val req = CreateCredentialsRequest.newBuilder().setSystem(SYSTEM_ENUM.CUSTOMER).setUserId(userId).build()

        authApiAsyncStub.createCredentials(req, object : StreamObserver<Void> {
            override fun onCompleted() {
                log.info { "[$userId] Credentials created successfully" }
            }

            override fun onError(t: Throwable?) {
                log.info { "[$userId] error while creating user credentials" }
                if(retry <= maxRetry ){
                    log.info { "[$userId] Trying again: $retry" }

                    // exponential backoff based on retry quantity
                    Thread.sleep(1000L * (retry + 1))

                    createCredentialsProxy(userId, retry + 1)

                } else {
                    log.info { "[$userId] Could not create user credentials. Removing user from database" }
                    customerRepository.deleteById(ObjectId(userId))
                }
            }

            override fun onNext(value: Void?) {}
        })
    }

    override fun deleteCredentials(userId: String) {
        deleteCredentialsProxy(userId, 0)
    }

    private fun deleteCredentialsProxy(userId: String, retry: Int){
        val req = RemoveCredentialsRequest.newBuilder().setSystem(SYSTEM_ENUM.CUSTOMER).setUserId(userId).build()

        authApiAsyncStub.deleteCredentials(req, object : StreamObserver<Void> {
            override fun onCompleted() {
                log.info { "[$userId] Credentials deleted successfully" }
            }

            override fun onError(t: Throwable?) {
                log.info { "[$userId] error while deleting user credentials" }
                if(retry <= maxRetry ){
                    log.info { "[$userId] Trying again: $retry" }

                    // exponential backoff based on retry quantity
                    Thread.sleep(1000L * (retry + 1))

                    deleteCredentialsProxy(userId, retry + 1)

                } else {
                    log.info { "[$userId] Could not delete user credentials." }
                }
            }

            override fun onNext(value: Void?) {}
        })
    }
}