package br.com.argus.service

import br.com.argus.rest.client.UsersResourceClient
import br.com.argus.service.impl.IdpServiceImpl
import io.mockk.every
import io.mockk.verify
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.Assertions
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
    private lateinit var usersResourceClient: UsersResourceClient

    @Test
    fun `createUser - should create user in idp server successfully`() {
        val response = Response.created(URI("http://keycloak.internal/admin/realms/testrealm/users/useridpid")).build()
        every { usersResourceClient.createUser(any()) } returns Uni.createFrom().item(response)

        idpService.createUser("email@email.com", "password123")
            .invoke{idpId -> assertEquals("useridpid", idpId)}
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
    }

    @Test
    fun `deleteUser - should delete user in idp server successfully`() {
        val expected = "useridpid"

        every { usersResourceClient.deleteUser(expected) } returns Uni.createFrom().item(Unit)

        idpService.deleteUser(expected)
            .invoke { it -> verify(exactly = 1) { usersResourceClient.deleteUser(expected)  } }
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
    }

}