package br.com.argus.rest.client

import br.com.argus.dto.UserRepresentation
import br.com.argus.rest.BearerTokenHeaderFactory
import io.smallrye.faulttolerance.api.ExponentialBackoff
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.DELETE
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.Response

@Path("/users")
@RegisterRestClient
@RegisterClientHeaders(BearerTokenHeaderFactory::class)
interface UsersResourceClient {

    @POST
    @Retry(maxRetries = 3, delay = 1)
    @ExponentialBackoff
    fun createUser(userRepresentation: UserRepresentation): Uni<Response>

    @DELETE
    @Path("/{idpId}")
    @Retry(maxRetries = 3, delay = 1)
    @ExponentialBackoff
    fun deleteUser(idpId: String): Uni<Unit>


}