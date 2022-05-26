package br.com.argus.customer_backend.controllers

import br.com.argus.customer_backend.dto.CustomerRequestDTO
import br.com.argus.customer_backend.dto.CustomerResponseDTO
import br.com.argus.customer_backend.dto.ErrorResponseDTO
import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import br.com.argus.customer_backend.services.CustomerService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.mongodb.MongoWriteException
import com.mongodb.ServerAddress
import com.mongodb.WriteError
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.bson.BsonDocument
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.lang.RuntimeException
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [CustomerController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = arrayOf(WebSecurityConfigurer::class)
    )],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        SecurityFilterAutoConfiguration::class,
        OAuth2ClientAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ])
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var customerService: CustomerService

    @MockkBean
    lateinit var passwordEncoderMock: PasswordEncoder

    private val passwordEncoder = BCryptPasswordEncoder()

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    @DisplayName("should return status code 201 and customer data when valid payload were given")
    fun testCreateCustomer201() {

        val request = CustomerRequestDTO(
            "77568872041",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
            "teste1.928",
        )

        every { customerService.save(any()) } returns Customer.from(request, passwordEncoder)
        every { passwordEncoderMock.encode(eq(request.password)) } returns passwordEncoder.encode(request.password)

        mockMvc.perform(
            post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cpf").isString)
            .andExpect(jsonPath("$.id").isString)
            .andExpect(jsonPath("$.email").isString)
            .andExpect(jsonPath("$.name").isString)
            .andExpect(jsonPath("$.profilePicUri").isString)
            .andExpect(jsonPath("$.favs").isMap)
    }

    @Test
    @DisplayName("should return status code 400 and error responsew with code 001 when incomplete payload were given")
    fun testCreateCustomer400() {

        val request = CustomerRequestDTO(
            "77568872",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
        )

        every { customerService.save(any()) } returns Customer.from(request, passwordEncoder)

        val res = mockMvc.perform(
            post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val errorRes = mapper.readValue<ErrorResponseDTO>(res)

        assertEquals("001", errorRes.code)
        assertNotNull(errorRes.message)
    }

    @Test
    @DisplayName("should return status code 409 when provided customer already exists in database")
    fun testCreateCustomer409() {

        val request = CustomerRequestDTO(
            "77568872041",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
            "teste1.928",
        )

        every { customerService.save(any()) } throws MongoWriteException(
            WriteError(0, "error", BsonDocument()),
            ServerAddress()
        )
        every { passwordEncoderMock.encode(eq(request.password)) } returns passwordEncoder.encode(request.password)


        val res = mockMvc.perform(
            post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val errorRes = mapper.readValue<ErrorResponseDTO>(res)

        assertEquals("002", errorRes.code)
        assertNotNull(errorRes.message)
    }

    @Test
    @DisplayName("should return status code 500 when unknown error occur")
    fun testCreateCustomer500() {

        val request = CustomerRequestDTO(
            "77568872041",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
            "teste1.928",
        )

        every { customerService.save(any()) } throws RuntimeException("generic expression")

        val res = mockMvc.perform(
            post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is5xxServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val errorRes = mapper.readValue<ErrorResponseDTO>(res)

        assertEquals("099", errorRes.code)
        assertNotNull(errorRes.message)
    }

    @Test
    @DisplayName("should return customer info when valid and existent customer ID were given")
    fun testGetCustomerById200() {

        val id = ObjectId.get()

        val customer = Customer(id, "15428547548", "Generic Name", "email@email.com", "passwordhash", "", HashMap())

        every { customerService.findOne(id = any()) } returns customer

        val res = mockMvc.perform(
            get("/customers/" + id.toHexString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cpf").isString)
            .andExpect(jsonPath("$.id").isString)
            .andExpect(jsonPath("$.email").isString)
            .andExpect(jsonPath("$.name").isString)
            .andExpect(jsonPath("$.profilePicUri").isString)
            .andExpect(jsonPath("$.favs").isMap)
            .andReturn()
            .response
            .contentAsString

        val customerRes = mapper.readValue<CustomerResponseDTO>(res)

        assertEquals(customer.id.toHexString(), customerRes.id)
    }

    @Test
    @DisplayName("should return 404 error with code 003 when given ID does not exists in database")
    fun testGetCustomerById404() {

        every { customerService.findOne(id = any()) } throws NoSuchElementException()

        val raw = mockMvc.perform(
            get("/customers/${ObjectId.get()}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        assertEquals("003", res.code)
    }

    @Test
    @DisplayName("should return customer info when valid and existent customer CPF were given")
    fun testGetCustomerByCPF200() {

        val customer =
            Customer(ObjectId(), "72646019513", "Generic Name", "email@email.com", "passwordhash", "", HashMap())

        every { customerService.findOne(cpf = any()) } returns customer

        val res = mockMvc.perform(
            get("/customers?cpf=" + customer.cpf)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cpf").isString)
            .andExpect(jsonPath("$.id").isString)
            .andExpect(jsonPath("$.email").isString)
            .andExpect(jsonPath("$.name").isString)
            .andExpect(jsonPath("$.profilePicUri").isString)
            .andExpect(jsonPath("$.favs").isMap)
            .andReturn()
            .response
            .contentAsString

        val customerRes = mapper.readValue<CustomerResponseDTO>(res)

        assertEquals(customer.cpf, customerRes.cpf)
    }

    @Test
    @DisplayName("should return 404 error with code 003 when given cpf does not own a correspondent customer registered in database")
    fun testGetCustomerByCPF404() {

        every { customerService.findOne(cpf = any()) } throws NoSuchElementException()

        val raw = mockMvc.perform(
            get("/customers?cpf=72646019513")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        assertEquals("003", res.code)
    }

    @Test
    @DisplayName("should return 400 error with code 001 when given cpf are invalid or malformed")
    fun testGetCustomerByCPF400() {

        val raw = mockMvc.perform(
            get("/customers?cpf=testecpf")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        assertEquals("001", res.code)
    }

    @Test
    @DisplayName("should return customer info when valid and existent customer Email were given")
    fun testGetCustomerByEmail200() {

        val customer =
            Customer(ObjectId(), "72646019513", "Generic Name", "email@email.com", "passwordhash", "", HashMap())

        every { customerService.findOne(email = any()) } returns customer

        val res = mockMvc.perform(
            get("/customers?email=" + customer.email)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cpf").isString)
            .andExpect(jsonPath("$.id").isString)
            .andExpect(jsonPath("$.email").isString)
            .andExpect(jsonPath("$.name").isString)
            .andExpect(jsonPath("$.profilePicUri").isString)
            .andExpect(jsonPath("$.favs").isMap)
            .andReturn()
            .response
            .contentAsString

        val customerRes = mapper.readValue<CustomerResponseDTO>(res)

        assertEquals(customer.email, customerRes.email)
    }

    @Test
    @DisplayName("should return 404 error with code 003 when given email does not own a correspondent customer registered in database")
    fun testGetCustomerByEmail404() {

        every { customerService.findOne(email = any()) } throws NoSuchElementException()

        val raw = mockMvc.perform(
            get("/customers?email=teste2@email.com")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        assertEquals("003", res.code)
    }

    @Test
    @DisplayName("should return customers when required params were given")
    fun testQueryCustomersByName() {

        val customers = listOf(
            Customer(ObjectId(), "54215478545", "Generic Name 2", "email2@email.com", "passwordhash", "", HashMap()),
            Customer(ObjectId(), "72646019513", "Generic Name", "email@email.com", "passwordhash", "", HashMap()),
        )

        every { customerService.findMany(name = any(), any()) } returns PageImpl(customers)

        val res = mockMvc.perform(
            get("/customers?name=Generic")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").isNumber)
            .andExpect(jsonPath("$.totalPages").isNumber)
            .andExpect(jsonPath("$.content").isArray)
            .andReturn()
            .response
            .contentAsString

        println(res)

        val pageRes = mapper.readValue<JsonNode>(res)

        assertEquals(2, pageRes.get("totalElements").asInt())
        assertEquals(1, pageRes.get("totalPages").asInt())
        assertEquals(2, (pageRes.get("content") as ArrayNode).size())
    }

    @Test
    @DisplayName("should return 400 with error code 004 when invalid direction were given")
    fun testQueryCustomersByName400() {

        val customers = listOf(
            Customer(ObjectId(), "54215478545", "Generic Name 2", "email2@email.com", "passwordhash", "", HashMap()),
            Customer(ObjectId(), "72646019513", "Generic Name", "email@email.com", "passwordhash", "", HashMap()),
        )

        every { customerService.findMany(name = any(), any()) } returns PageImpl(customers)

        val raw = mockMvc.perform(
            get("/customers?name=Generic&order=FRE")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").isString)
            .andExpect(jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        assertEquals("004", res.code)

    }

}