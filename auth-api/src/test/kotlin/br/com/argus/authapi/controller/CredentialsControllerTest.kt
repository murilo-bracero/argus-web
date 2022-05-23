package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.CreateUserCredentialsDTO
import br.com.argus.authapi.dto.UpdateUserCredentialsDTO
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.service.CredentialsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [CredentialsController::class])
class CredentialsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var service: CredentialsService

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun funCreateCredentials201(){

        every { service.createCredential(any()) } returns Unit

        val req = CreateUserCredentialsDTO(ObjectId.get().toHexString(), SystemEnum.CUSTOMER)

        mockMvc.perform(post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated)

        verify(exactly = 1) { service.createCredential(any()) }
    }

    @Test
    @DisplayName("should return http status 404 and error code 002 when provided user id is inexistent")
    fun funCreateCredentials404(){

        every { service.createCredential(any()) } throws NoSuchElementException()

        val req = CreateUserCredentialsDTO(ObjectId.get().toHexString(), SystemEnum.UNKNOWN)

        mockMvc.perform(post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("002"))
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun funCreateCredentials400(){
        val req = CreateUserCredentialsDTO()

        mockMvc.perform(post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("001"))
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun funCreateCredentials503(){

        every { service.createCredential(any()) } throws RuntimeException()

        val req = CreateUserCredentialsDTO(ObjectId.get().toHexString(), SystemEnum.CUSTOMER)

        mockMvc.perform(post("/credentials").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("099"))
            .andExpect(jsonPath("$.message").isString)

        verify(exactly = 1) { service.createCredential(any()) }
    }

    @Test
    @DisplayName("should return 200 and no body when mfaEnabled remains false")
    fun funUpdateCredentials200(){

        val userId = ObjectId.get().toHexString()

        every { service.update(any()) } returns null
        every { service.find(eq(userId),eq(SystemEnum.CUSTOMER)) } returns UserCredential(id = ObjectId.get(), userId = userId)

        val req = UpdateUserCredentialsDTO(
            SystemEnum.CUSTOMER,
            mfaEnabled = false,
            accountExpired = true,
            accountLocked = false,
            credentialsExpired = false,
            isEnabled = true
        )

        mockMvc.perform(patch("/credentials/$userId").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secret").doesNotExist())

        verify(exactly = 1) { service.update(any()) }
        verify(exactly = 1) { service.find(eq(userId),eq(SystemEnum.CUSTOMER)) }
    }

    @Test
    @DisplayName("should return 200 and body with secret field if mfaEnabled is set to true in payload")
    fun funUpdateCredentials200Secret(){

        val userId = ObjectId.get().toHexString()

        every { service.update(any()) } returns "secret"
        every { service.find(eq(userId),eq(SystemEnum.CUSTOMER)) } returns UserCredential(id = ObjectId.get(), userId = userId)

        val req = UpdateUserCredentialsDTO(
            SystemEnum.CUSTOMER,
            mfaEnabled = true,
            accountExpired = true,
            accountLocked = false,
            credentialsExpired = false,
            isEnabled = true
        )

        mockMvc.perform(patch("/credentials/$userId").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secret").value("secret"))

        verify(exactly = 1) { service.update(any()) }
        verify(exactly = 1) { service.find(eq(userId),eq(SystemEnum.CUSTOMER)) }
    }

    @Test
    @DisplayName("should return 400 when no user system are provided at given payload")
    fun funUpdateCredentials400(){

        val userId = ObjectId.get().toHexString()

        val req = UpdateUserCredentialsDTO(
            mfaEnabled = true,
            accountExpired = true,
            accountLocked = false,
            credentialsExpired = false,
            isEnabled = true
        )

        mockMvc.perform(patch("/credentials/$userId").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("001"))
            .andExpect(jsonPath("$.message").isString)
    }
}