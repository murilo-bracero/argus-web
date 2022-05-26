package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.UpdateUserCredentialsDTO
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.utils.JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [CredentialsController::class])
@AutoConfigureMockMvc
class CredentialsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtUtils: JwtUtils

    @MockkBean
    lateinit var service: CredentialsService

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    @DisplayName("should return 200 and no body when mfaEnabled remains false")
    fun funUpdateCredentials200() {

        val userId = ObjectId.get().toHexString()

        val mockClaims = HashMap<String, String>()
        mockClaims["id"] = userId
        mockClaims["system"] = "CUSTOMER"

        every { jwtUtils.verify(any()) } returns mockClaims

        every { service.update(any()) } returns null
        every { service.find(eq(userId), eq(SystemEnum.CUSTOMER)) } returns UserCredential(
            id = ObjectId.get(),
            userId = userId,
            system = SystemEnum.CUSTOMER
        )

        val req = UpdateUserCredentialsDTO(
            mfaEnabled = false,
            accountExpired = true,
            accountLocked = false,
            credentialsExpired = false,
            isEnabled = true
        )

        mockMvc.perform(
            patch("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secret").doesNotExist())

        verify(exactly = 1) { service.update(any()) }
        verify(exactly = 2) { service.find(eq(userId), eq(SystemEnum.CUSTOMER)) }
    }

    @Test
    @DisplayName("should return 200 and body with secret field if mfaEnabled is set to true in payload")
    fun funUpdateCredentials200Secret() {

        val userId = ObjectId.get().toHexString()

        val mockClaims = HashMap<String, String>()
        mockClaims["id"] = userId
        mockClaims["system"] = "CUSTOMER"

        every { jwtUtils.verify(any()) } returns mockClaims

        every { service.update(any()) } returns "secret"
        every { service.find(eq(userId), eq(SystemEnum.CUSTOMER)) } returns UserCredential(
            id = ObjectId.get(),
            userId = userId,
            system = SystemEnum.CUSTOMER
        )

        val req = UpdateUserCredentialsDTO(
            mfaEnabled = true,
            accountExpired = true,
            accountLocked = false,
            credentialsExpired = false,
            isEnabled = true
        )

        mockMvc.perform(
            patch("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secret").value("secret"))

        verify(exactly = 1) { service.update(any()) }
        verify(exactly = 2) { service.find(eq(userId), eq(SystemEnum.CUSTOMER)) }
    }
}