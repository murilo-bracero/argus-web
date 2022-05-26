package br.com.argus.customer_backend.controllers

import br.com.argus.customer_backend.dto.CustomerRequestDTO
import br.com.argus.customer_backend.dto.CustomerResponseDTO
import br.com.argus.customer_backend.dto.ErrorResponseDTO
import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bson.types.ObjectId
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.collections.HashMap

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
class CustomerControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    private val mapper: ObjectMapper = ObjectMapper()

    lateinit var customers: List<Customer>

    @BeforeAll
    fun setUp(){

        customers = listOf(
            Customer(ObjectId(), "74576532014", "Generic Name", "generic.mail@mail.com", "generic password hash", "", HashMap()),
            Customer(ObjectId(), "08729096030", "Generic Name Two", "generic.mail1@mail.com", "generic password hash1", "", HashMap()),
            Customer(ObjectId(), "47761904006", "Alternate Name Three", "generic.mail2@mail.com", "generic password hash2", "", HashMap()),
        )

        customers.map {
            println("creating ${it.cpf}")
            customerRepository.save(it)
        }
    }

    @Test
    @DisplayName("should return status code 201 and customer data when valid payload were given")
    fun testCreateCustomer201() {

        val request = CustomerRequestDTO(
            "77568872041",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
            "teste1.928",
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.profilePicUri").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.favs").isMap)
    }

    @Test
    @DisplayName("should return status code 400 and error responsew with code 001 when incomplete payload were given")
    fun testCreateCustomer400() {

        val request = CustomerRequestDTO(
            "77568872",
            "Arnaldo Franco",
            "arnaldo.franco@email.com",
        )

        val res = mockMvc.perform(MockMvcRequestBuilders.post("/customers").content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val errorRes = mapper.readValue<ErrorResponseDTO>(res)

        Assertions.assertEquals("001", errorRes.code)
        Assertions.assertNotNull(errorRes.message)
    }

    @Test
    @DisplayName("should return customer info when valid and existent customer ID were given")
    fun testGetCustomerById200() {
        val customer = customers.first()

        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers/" + customer.id.toHexString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.profilePicUri").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.favs").isMap)
            .andReturn()
            .response
            .contentAsString

        val customerRes = mapper.readValue<CustomerResponseDTO>(res)

        Assertions.assertEquals(customer.id.toHexString(), customerRes.id)
    }

    @Test
    @DisplayName("should return 404 error with code 003 when given ID does not exists in database")
    fun testGetCustomerById404() {

        val raw = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers/${ObjectId.get()}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        Assertions.assertEquals("003", res.code)
    }

    @Test
    @DisplayName("should return customer info when valid and existent customer CPF were given")
    fun testGetCustomerByCPF200() {

        val customer = customers.first()

        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?cpf=" + customer.cpf)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.profilePicUri").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.favs").isMap)
            .andReturn()
            .response
            .contentAsString

        val customerRes = mapper.readValue<CustomerResponseDTO>(res)

        Assertions.assertEquals(customer.cpf, customerRes.cpf)
    }

    @Test
    @DisplayName("should return 404 error with code 003 when given cpf does not own a correspondent customer registered in database")
    fun testGetCustomerByCPF404() {

        val raw = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?cpf=95869352053")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        Assertions.assertEquals("003", res.code)
    }

    @Test
    @DisplayName("should return 400 error with code 001 when given cpf are invalid or malformed")
    fun testGetCustomerByCPF400() {

        val raw = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?cpf=testecpf")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        Assertions.assertEquals("001", res.code)
    }

    @Test
    @DisplayName("should return customer info when valid and existent customer Email were given")
    fun testGetCustomerByEmail200() {

        val customer = customers.first()

        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?email=" + customer.email)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.profilePicUri").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.favs").isMap)
            .andReturn()
            .response
            .contentAsString

        val customerRes = mapper.readValue<CustomerResponseDTO>(res)

        Assertions.assertEquals(customer.email, customerRes.email)
    }
    @Test
    @DisplayName("should return 404 error with code 003 when given email does not own a correspondent customer registered in database")
    fun testGetCustomerByEmail404() {

        val raw = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?email=teste2@email.com")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        Assertions.assertEquals("003", res.code)
    }

    @Test
    @DisplayName("should return customers when required params were given")
    fun testQueryCustomersByName() {

        mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?name=Generic")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andReturn()
            .response
            .contentAsString
    }

    @Test
    @DisplayName("should return a customers page when none params were given")
    fun testQueryAllCustomers() {

        mockMvc.perform(
            MockMvcRequestBuilders.get("/customers")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andReturn()
            .response
            .contentAsString
    }

    @Test
    @DisplayName("should return 400 with error code 004 when invalid direction were given")
    fun testQueryCustomersByName400() {

        val raw = mockMvc.perform(
            MockMvcRequestBuilders.get("/customers?name=Generic&order=FRE")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
            .andReturn()
            .response
            .contentAsString

        val res = mapper.readValue<ErrorResponseDTO>(raw)

        Assertions.assertEquals("004", res.code)

    }


}