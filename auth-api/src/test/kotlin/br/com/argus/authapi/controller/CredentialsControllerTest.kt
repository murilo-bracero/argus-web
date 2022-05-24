package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.CreateUserCredentialsDTO
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.Base64Utils
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

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
    fun funCreateCredentials201() {

        mockFilter()

        every { service.createCredential(any()) } returns Unit

        val req = CreateUserCredentialsDTO(ObjectId.get().toHexString(), SystemEnum.CUSTOMER)

        mockMvc.perform(
            post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer token")
        )
            .andExpect(status().isCreated)

        verify(exactly = 1) { service.createCredential(any()) }
    }

    @Test
    @DisplayName("should return http status 404 and error code 002 when provided user id is inexistent")
    fun funCreateCredentials404() {

        mockFilter()

        every { service.createCredential(any()) } throws NoSuchElementException()

        val req = CreateUserCredentialsDTO(ObjectId.get().toHexString(), SystemEnum.UNKNOWN)

        mockMvc.perform(
            post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer token")
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("002"))
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun funCreateCredentials400() {
        val req = CreateUserCredentialsDTO()

        mockFilter()

        mockMvc.perform(
            post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer token")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("001"))
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun funCreateCredentials503() {

        mockFilter()

        every { service.createCredential(any()) } throws RuntimeException()

        val req = CreateUserCredentialsDTO(ObjectId.get().toHexString(), SystemEnum.CUSTOMER)

        mockMvc.perform(
            post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer token")
        )
            .andExpect(status().isServiceUnavailable)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("099"))
            .andExpect(jsonPath("$.message").isString)

        verify(exactly = 1) { service.createCredential(any()) }
    }

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

    fun mockFilter(){
        val mockClaims = HashMap<String, String>()
        mockClaims["id"] = "mockedId"
        mockClaims["system"] = "CUSTOMER"

        val creds = UserCredential(ObjectId.get(), "mockedId", system = SystemEnum.CUSTOMER)

        every { jwtUtils.verify(any()) } returns mockClaims
        every { service.find(eq("mockedId"), eq(SystemEnum.CUSTOMER)) } returns creds
    }
}