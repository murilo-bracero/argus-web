package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.CustomerResponseDTO
import br.com.argus.authapi.dto.LoginRequestDTO
import br.com.argus.authapi.exception.AuthNeedsMfaException
import br.com.argus.authapi.model.*
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.service.CustomerService
import br.com.argus.authapi.service.UserAuthenticationService
import br.com.argus.authapi.utils.JwtUtils
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath


@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [LoginController::class])
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtUtils: JwtUtils

    @MockkBean
    lateinit var credentialsService: CredentialsService

    @MockkBean
    lateinit var service: UserAuthenticationService

    @MockkBean lateinit var customerService: CustomerService

    private lateinit var mapper: ObjectMapper
    private lateinit var customer: CustomerResponseDTO
    private lateinit var user: User

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
        user = User(
            ObjectId(customer.id),
            customer.email,
            "hashpassword"
            )

        user.credentials = UserCredential(
            ObjectId.get(),
            user.id.toHexString(),
        )
    }

    @Test
    @DisplayName("Should return 200 and user information if provided credentials without mfa are correct")
    fun login200(){
        val req = LoginRequestDTO(customer.email, "genericPassword", SystemEnum.CUSTOMER)

        every { jwtUtils.generateToken(eq(customer.id), eq(SystemEnum.CUSTOMER)) } returns Tokens("mocked_access_token", "mocked_refresh_token")
        every { service.login(eq(req.email), eq(req.password), null, eq(SystemEnum.CUSTOMER)) } returns user
        every { customerService.findByEmail(eq(req.email)) } returns customer
        every { credentialsService.update(any()) } returns ""

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.user.cpf").exists())
            .andExpect(jsonPath("$.user.name").exists())
            .andExpect(jsonPath("$.user.email").exists())
            .andExpect(jsonPath("$.user.email").value(req.email))
            .andExpect(jsonPath("$.user.profilePicUri").exists())
            .andExpect(jsonPath("$.user.favs").exists())

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

        every { service.login(eq(req.email), eq(req.password), null, eq(SystemEnum.CUSTOMER)) } throws BadCredentialsException("invalid credentials")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @DisplayName("Should return 401 with no header if login credentials are right but mfa token is missing")
    fun login401LoginMfa(){
        val req = LoginRequestDTO("generic.email@email.com", "genericpass", SystemEnum.CUSTOMER)

        every { service.login(eq(req.email), eq(req.password), null, eq(SystemEnum.CUSTOMER)) } throws AuthNeedsMfaException()

        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
            .response

        assertNotNull(res.getHeader("WWW-Authenticate"))
        assertEquals("authType=\"TOTP\"", res.getHeader("WWW-Authenticate"))
    }

    @Test
    @DisplayName("Should return 503 when a unknown error occur")
    fun login503(){

        every { service.login(any(), any(), null, eq(SystemEnum.CUSTOMER)) } throws RuntimeException()

        val req = LoginRequestDTO("generic.email@email.com", "genericPassword", SystemEnum.CUSTOMER)

        mockMvc.perform(MockMvcRequestBuilders.post("/auth").content(mapper.writeValueAsString(req)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isServiceUnavailable)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("099"))
            .andExpect(jsonPath("$.message").isString)

        verify(exactly = 1) { service.login(any(), any(), null, eq(SystemEnum.CUSTOMER)) }
    }

    @Test
    @DisplayName("Should return 200 and new access and refresh tokens when provided refresh token are valid")
    fun refresh200(){
        val refreshToken = "mocked_refresh_token"
        val creds = UserCredential()
        creds.system = SystemEnum.TECHNICIAN
        creds.refreshToken = refreshToken

        val mockedClaims = HashMap<String, String>()
        mockedClaims["id"] = creds.id.toHexString()
        mockedClaims["system"] = "TECHNICIAN"

        every { jwtUtils.verify(eq(refreshToken)) } returns mockedClaims
        every { jwtUtils.generateToken(eq(creds.id.toHexString()), eq(SystemEnum.TECHNICIAN)) } returns Tokens("mocked_new_access_token", "mocked_new_refresh_token")
        every { credentialsService.find(eq(creds.id.toHexString()), eq(SystemEnum.TECHNICIAN)) } returns creds
        every { credentialsService.update(any()) } returns ""

        mockMvc.perform(
            MockMvcRequestBuilders.get("/auth/refresh").queryParam("refreshToken", refreshToken))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").value("mocked_new_refresh_token"))
    }

    @Test
    @DisplayName("Should return 401 when refresh token is malformed")
    fun refresh401Malformed(){
        val refreshToken = "mocked_refresh_token"
        val creds = UserCredential()
        creds.system = SystemEnum.TECHNICIAN
        creds.refreshToken = refreshToken

        val mockedClaims = HashMap<String, String>()
        mockedClaims["id"] = creds.id.toHexString()

        every { jwtUtils.verify(eq(refreshToken)) } returns mockedClaims

        mockMvc.perform(
            MockMvcRequestBuilders.get("/auth/refresh").queryParam("refreshToken", refreshToken))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @DisplayName("Should return 401 when provided refresh token does not match with the stored one")
    fun refresh401Diff(){
        val refreshToken = "mocked_refresh_token"
        val creds = UserCredential()
        creds.system = SystemEnum.TECHNICIAN
        creds.refreshToken = refreshToken + "_diff"

        val mockedClaims = HashMap<String, String>()
        mockedClaims["id"] = creds.id.toHexString()
        mockedClaims["system"] = "TECHNICIAN"

        every { jwtUtils.verify(eq(refreshToken)) } returns mockedClaims
        every { credentialsService.find(eq(creds.id.toHexString()), eq(SystemEnum.TECHNICIAN)) } returns creds

        mockMvc.perform(
            MockMvcRequestBuilders.get("/auth/refresh").queryParam("refreshToken", refreshToken))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
}