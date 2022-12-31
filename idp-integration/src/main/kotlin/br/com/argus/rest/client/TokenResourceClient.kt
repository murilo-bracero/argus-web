package br.com.argus.rest.client;

import br.com.argus.dto.TokenRepresentation
import io.smallrye.faulttolerance.api.ExponentialBackoff
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.jboss.resteasy.reactive.RestForm
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/token")
@RegisterRestClient
interface TokenResourceClient {

    @POST
    @Retry(maxRetries = 3, delay = 1)
    @ExponentialBackoff
    fun getToken(@RestForm("grant_type") grantType: String, @HeaderParam("Authorization") authorization: String): Uni<TokenRepresentation>

}
