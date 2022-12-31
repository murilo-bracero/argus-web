package br.com.argus.service.impl

import br.com.argus.dto.CredentialsRepresentation
import br.com.argus.dto.UserRepresentation
import br.com.argus.rest.client.UsersResourceClient
import br.com.argus.service.IdpService
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.rest.client.inject.RestClient
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

@ApplicationScoped
class IdpServiceImpl(
    @Inject @RestClient private val usersResourceClient: UsersResourceClient
) : IdpService {

    companion object {
        private const val CREDENTIALS_TYPE = "password"
    }

    override fun createUser(email: String, password: String): Uni<String> {
        Log.info("Starting creating user flow")
        Log.debug("[email=$email, password=$password]")

        val request = createRequest(email, password)

        Log.info("Request created")
        Log.debug(request)

        Log.info("Sending request")
        return usersResourceClient.createUser(request)
            .onItem()
            .transform {
                Log.info("IDP service response success")
                Log.debug(it)
                return@transform findIdInResponse(it) }
    }

    private fun createRequest(email: String, password: String): UserRepresentation {
        val credentialsRepresentation = CredentialsRepresentation(
            CREDENTIALS_TYPE,
            password
        )

        return UserRepresentation(
            email,
            true,
            listOf(credentialsRepresentation)
        )
    }

    private fun findIdInResponse(response: Response): String{
        val locationUri = response.location

        return locationUri.path.split("/").last()
    }

    override fun deleteUser(idpId: String): Uni<Unit> {
        Log.info("Starting delete user in idp service flow for id $idpId")
        return usersResourceClient.deleteUser(idpId)
            .log("Removed user from idp service successfully")
    }
}