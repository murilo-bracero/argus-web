package br.com.argus.customer_backend.controllers

import br.com.argus.customer_backend.dto.CustomerRequestDTO
import br.com.argus.customer_backend.dto.CustomerResponseDTO
import br.com.argus.customer_backend.dto.ErrorResponseDTO
import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [CustomerController::class])
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var customerRepository: CustomerRepository

    val passwordEncoder = BCryptPasswordEncoder()

    val mapper: ObjectMapper = ObjectMapper()

    @Test
    @DisplayName("should return status code 201 and customer data when valid payload were given")
    fun testCreateCustomer201() {

        val request = CustomerRequestDTO(
            "77568872041",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
            "teste1.928",
        )

        every { customerRepository.save(any()) } returns Customer.from(request, passwordEncoder)

        mockMvc.perform(post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
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

        every { customerRepository.save(any()) } returns Customer.from(request, passwordEncoder)

        val res = mockMvc.perform(post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
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

        every { customerRepository.save(any()) } throws MongoWriteException(WriteError(0, "error", BsonDocument()), ServerAddress())

        val res = mockMvc.perform(post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
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

        every { customerRepository.save(any()) } throws RuntimeException("generic expression")

        val res = mockMvc.perform(post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
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

        every { customerRepository.findById(id) } returns Optional.of(customer)

        val res = mockMvc.perform(get("/customers/" + id.toHexString())
            .contentType(MediaType.APPLICATION_JSON))
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

        val id = ObjectId.get()

        every { customerRepository.findById(id) } returns Optional.empty()

        val raw = mockMvc.perform(get("/customers/" + id.toHexString())
            .contentType(MediaType.APPLICATION_JSON))
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
}