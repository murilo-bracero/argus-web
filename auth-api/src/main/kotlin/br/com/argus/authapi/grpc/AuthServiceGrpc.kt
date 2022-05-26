package br.com.argus.authapi.grpc

import br.com.argus.authapi.*
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.utils.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthServiceGrpc(
    @Autowired private val credentialsService: CredentialsService,
    @Autowired private val jwtUtils: JwtUtils
) : AuthApiServiceGrpcKt.AuthApiServiceCoroutineImplBase() {

    override suspend fun createCredentials(request: CreateCredentialsRequest): Void {
        credentialsService.createCredential(UserCredential.from(request))
        return Void.newBuilder().build()
    }

    override suspend fun validateToken(request: ValidateTokenRequest): Credentials {

        val claims = jwtUtils.verify(request.token)

        val id = Optional.ofNullable(claims["id"]).orElseThrow()
        val system = Optional.ofNullable(claims["system"])
            .map { SystemEnum.valueOf(it) }
            .orElseThrow()

        return credentialsService.find(id, system).to()
    }

    override suspend fun deleteCredentials(request: RemoveCredentialsRequest): Void {

        credentialsService.remove(request.userId, SystemEnum.valueOf(request.system.name))

        return Void.newBuilder().build()
    }
}