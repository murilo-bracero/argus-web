package br.com.argus.service

import br.com.argus.rest.client.KeycloakResourceClient
import br.com.argus.service.impl.IdpServiceImpl
import io.mockk.every
import io.mockk.verify
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.resteasy.reactive.common.jaxrs.ResponseImpl
import org.jboss.resteasy.reactive.common.util.MultivaluedTreeMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import javax.inject.Inject
import javax.ws.rs.core.Response

@QuarkusTest
class IdpServiceTest {

    @Inject
    private lateinit var idpService: IdpServiceImpl

    @InjectMock
    @RestClient
    private lateinit var keycloakResourceClient: KeycloakResourceClient

    @Test
    fun `createUser - should create user in idp server successfully`() {
        val response = Response.created(URI("http://keycloak.internal/admin/realms/testrealm/users/useridpid")).build()
        every { keycloakResourceClient.createUser(any()) } returns response

        val idpUser = idpService.createUser("email@email.com", "password123")

        assertEquals("useridpid", idpUser)
    }

    @Test
    fun `deleteUser - should delete user in idp server successfully`() {
        val expected = "useridpid"

        every { keycloakResourceClient.deleteUser(expected) } returns Unit

        idpService.deleteUser(expected)

        verify(exactly = 1) { keycloakResourceClient.deleteUser(expected)  }
    }

}