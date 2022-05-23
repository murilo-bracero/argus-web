package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.CustomerResponseDTO
import br.com.argus.authapi.dto.LoginRequestDTO
import br.com.argus.authapi.model.AuthResultEnum
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.service.CustomerService
import br.com.argus.authapi.service.UserAthenticationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.lang.RuntimeException

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [LoginController::class])
class LoginControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var service: UserAthenticationService

    @MockkBean lateinit var customerService: CustomerService

    private lateinit var mapper: ObjectMapper
    private lateinit var customer: CustomerResponseDTO

    @BeforeEach
    fun setUp(){
        mapper = ObjectMapper()
        customer = CustomerResponseDTO(
            ObjectId.get().toHexString(),
            "33142523073",
            "generic.email@mail.com",
            "Generic Name",
            "",
            HashMap()
        )
    }

    @Test
    @DisplayName("Should return 200 and user information if provided credentials without mfa are correct")
    fun login200(){

        println(mapper.writeValueAsString(customer))

        val req = LoginRequestDTO(customer.email, "genericPassword", SystemEnum.CUSTOMER)

        every { service.login(eq(req.email), eq(req.password), null, eq(SystemEnum.CUSTOMER)) } returns AuthResultEnum.AUTHENTICATED
        every { customerService.findByEmail(eq(req.email)) } returns customer

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.cpf").exists())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.email").value(req.email))
            .andExpect(jsonPath("$.profilePicUri").exists())
            .andExpect(jsonPath("$.favs").exists())

        verify(exactly = 1) { service.login(any(), any(), null, eq(SystemEnum.CUSTOMER)) }
    }

    @Test
    @DisplayName("Should return 400 if provided payload are incomplete or invalid")
    fun login400(){
        val req = LoginRequestDTO("generic.email@email.com", "", SystemEnum.UNKNOWN)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("001"))
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    @DisplayName("Should return 401 with no header if login credentials are wrong")
    fun login401Login(){
        val req = LoginRequestDTO("generic.email@email.com", "genericpass", SystemEnum.CUSTOMER)

        every { service.login(eq(req.email), eq(req.password), null, eq(SystemEnum.CUSTOMER)) } returns AuthResultEnum.LOGIN_DENIED

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @DisplayName("Should return 401 with no header if login credentials are right but mfa token is missing")
    fun login401LoginMfa(){
        val req = LoginRequestDTO("generic.email@email.com", "genericpass", SystemEnum.CUSTOMER)

        every { service.login(eq(req.email), eq(req.password), null, eq(SystemEnum.CUSTOMER)) } returns AuthResultEnum.NEED_MFA

        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
            .response

        assertNotNull(res.getHeader("WWW-Authenticate"))
        assertEquals("authType=\"TOTP\"", res.getHeader("WWW-Authenticate"))
    }

    @Test
    fun funCreateCredentials503(){

        every { service.login(any(), any(), null, eq(SystemEnum.CUSTOMER)) } throws RuntimeException()

        val req = LoginRequestDTO("generic.email@email.com", "genericPassword", SystemEnum.CUSTOMER)

        mockMvc.perform(MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isServiceUnavailable)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("099"))
            .andExpect(jsonPath("$.message").isString)

        verify(exactly = 1) { service.login(any(), any(), null, eq(SystemEnum.CUSTOMER)) }
    }
}