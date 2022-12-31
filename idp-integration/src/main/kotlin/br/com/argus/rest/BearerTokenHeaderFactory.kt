package br.com.argus.rest

import br.com.argus.rest.client.TokenResourceClient
import io.quarkus.logging.Log
import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap


@ApplicationScoped
class BearerTokenHeaderFactory(
    @Inject @RestClient private val tokenResourceClient: TokenResourceClient,
    @ConfigProperty(name = "IDP_APP_NAME") private val idpAppName: String,
    @ConfigProperty(name = "IDP_SERVICE_ACCOUNT") private val idpServiceAccount: String
) : ReactiveClientHeadersFactory() {

    override fun getHeaders(
        incomingHeaders: MultivaluedMap<String, String>?,
        clientOutgoingHeaders: MultivaluedMap<String, String>?
    ): Uni<MultivaluedMap<String, String>> {

        val basic = Base64.getEncoder().encodeToString("$idpAppName:$idpServiceAccount".toByteArray())

        return tokenResourceClient.getToken("client_credentials", "Basic $basic")
            .onItem()
            .transform {
                val result: MultivaluedMap<String, String> = MultivaluedHashMap()

                result.add("Authorization", "Bearer ${it.accessToken}")
                return@transform result
            }
            .onFailure()
            .transform {
                Log.error("An error occurred while retrieving token from IDP Server", it)
                return@transform it
            }

    }
}