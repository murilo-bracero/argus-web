package br.com.argus.endpoint

import br.com.argus.CreateUserRequest
import br.com.argus.CreateUserResponse
import br.com.argus.DeleteUserRequest
import br.com.argus.IdpIntegrationService
import br.com.argus.Void
import br.com.argus.exception.IdpIntegrationException
import br.com.argus.service.IdpService
import io.quarkus.grpc.GrpcService
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import javax.inject.Inject

@GrpcService
class GrpcIdpIntegrationService(
    @Inject private val idpService: IdpService
): IdpIntegrationService {

    @Blocking
    override fun createUser(request: CreateUserRequest?): Uni<CreateUserResponse> {
        if(request == null){
            throw IdpIntegrationException("IDP001", "Provided request is null")
        }

        if(request.email.isNullOrEmpty() || request.password.isNullOrEmpty()){
            throw IdpIntegrationException("IDP002", "Email or Password must not be null")
        }

        val idpId = idpService.createUser(request.email, request.password)

        return Uni.createFrom().item{
            CreateUserResponse.newBuilder().setIdpId(idpId).build()
        }
    }

    @Blocking
    override fun deleteUser(request: DeleteUserRequest?): Uni<Void> {
        if(request == null){
            throw IdpIntegrationException("IDP001", "Provided request is null")
        }

        if(request.idpId.isNullOrEmpty()){
            throw IdpIntegrationException("IDP003", "idpId must not be null")
        }

        idpService.deleteUser(request.idpId)

        return Uni.createFrom().item { Void.newBuilder().build() }
    }
}