package br.com.argus.customerbackend.controllers

import br.com.argus.customerbackend.dto.CreateCustomerRequest
import br.com.argus.customerbackend.dto.CustomerResponse
import br.com.argus.customerbackend.models.Customer
import br.com.argus.customerbackend.services.CustomerService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(
    controllers = [CustomerController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class,
        SecurityFilterAutoConfiguration::class,
        OAuth2ClientAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class]
)
class CustomerControllerTest {

    @MockkBean
    lateinit var customerService: CustomerService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var customersList: List<Customer>

    @BeforeEach
    fun setUp(){
        customersList = listOf(
            createCustomer("85245695175"),
            createCustomer("75195386248"),
            createCustomer("71548569532"))
    }

    @Test
    fun `should return status code 201 and customer data when valid payload were given`() {

        val request = createCustomerRequest()

        every { customerService.save(any()) } returns request.toModel()

        val rawRes = mockMvc.post("/customers") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = APPLICATION_JSON
        }.andExpect {
                status { isCreated() }
            }
            .andReturn().response.contentAsString

        val customerResponse = objectMapper.readValue<CustomerResponse>(rawRes)

        assertEquals(request.cpf, customerResponse.cpf)
        assertEquals(request.name, customerResponse.name)
        assertEquals(request.email, customerResponse.email)
        assertNotNull(customerResponse.id)
    }

    @Test
    fun `should return status code 400 when incomplete payload were given`() {

        val request = CreateCustomerRequest(name = "Test")

        mockMvc.post("/customers") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return status code 400 when given cpf is invalid`() {

        val request = createCustomerRequest("12345678901")

        mockMvc.post("/customers") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return status code 200 and a list of customers`() {
        every { customerService.findAll(any()) } returns PageImpl(customersList)

        val rawRes = mockMvc.get("/customers") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val res = objectMapper.readValue<JsonNode>(rawRes)

        assertNotNull(res)
        assertEquals(customersList.size, res.get("numberOfElements").asInt())
        assertFalse(res.get("empty").asBoolean())
    }

    private fun createCustomerRequest(cpf: String = "77568872041"): CreateCustomerRequest {
        return CreateCustomerRequest(
            cpf,
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
            "teste1.928",
            "",
            "85974518595",
            "Test Avenue",
            845,
            "54852659",
            ""
        )
    }

    private fun createCustomer(cpf: String = "77568872041"): Customer {
        return createCustomerRequest(cpf).toModel()
    }
}