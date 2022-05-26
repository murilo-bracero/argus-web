package br.com.argus.customer_backend.services

import br.com.argus.customer_backend.models.Customer
import br.com.argus.customer_backend.repositories.CustomerRepository
import br.com.argus.customer_backend.services.impl.CustomerServiceImpl
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class CustomerServiceTest {

    @MockkBean
    private lateinit var customerRepository: CustomerRepository

    @MockkBean
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var customerService: CustomerServiceImpl

    private lateinit var customer: Customer

    @BeforeEach
    fun setUp(){
        val id = ObjectId.get()

        customer = Customer(
            id,
            "52637405066",
            id.toHexString(),
            "${id.toHexString()}@mail.com",
            passwordEncoder.encode(id.toHexString()),
            "",
            HashMap()
        )
    }

    @Test
    fun `should create new user`(){
        every { customerRepository.save(any()) } returns customer
        every { authenticationService.createCredentials(any()) } returns Unit

        val savedCustomer = customerService.save(customer)

        assertEquals(customer.id, savedCustomer.id)

        verify(exactly = 1) { authenticationService.createCredentials(any()) }
    }

    @Test
    fun `should retrieve user when valid cpf were given`(){
        every { customerRepository.findByCpf(any()) } returns Optional.of(customer)

        val savedCustomer = customerService.findOne(cpf=customer.cpf)

        assertEquals(customer.id, savedCustomer.id)
        assertEquals(customer.cpf, savedCustomer.cpf)

    }

    @Test
    fun `should throw NoSuchElementException when no customer with provided cpf were found`(){
        every { customerRepository.findByCpf(any()) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            customerService.findOne(cpf="54196584512")
        }
    }

    @Test
    fun `should retrieve user when valid email were given`(){
        every { customerRepository.findByEmail(any()) } returns Optional.of(customer)

        val savedCustomer = customerService.findOne(email=customer.email)

        assertEquals(customer.id, savedCustomer.id)
        assertEquals(customer.email, savedCustomer.email)
    }

    @Test
    fun `should throw NoSuchElementException when no customer with provided email were found`(){
        every { customerRepository.findByEmail(any()) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            customerService.findOne(email=customer.email)
        }
    }

    @Test
    fun `should retrieve user when valid id were given`(){
        every { customerRepository.findById(any()) } returns Optional.of(customer)

        val savedCustomer = customerService.findOne(id=customer.id)

        assertEquals(customer.id, savedCustomer.id)
    }

    @Test
    fun `should throw NoSuchElementException when no customer with provided id were found`(){
        every { customerRepository.findById(any()) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            customerService.findOne(id=customer.id)
        }
    }

    @Test
    fun `should remove user with provided id`(){
        every { customerRepository.deleteById(any()) } returns Unit

        customerService.delete(id=customer.id)

        verify(exactly = 1) { customerRepository.deleteById(any()) }
    }

    @Test
    fun `should return generic customers page when no name were given`(){
        every { customerRepository.findAll(any() as Pageable) } returns PageImpl(listOf(customer))

        val page = customerService.findMany(pageable = PageRequest.of(0, 10))

        assertEquals(1, page.totalPages)
        assertEquals(1, page.content.size)
    }

    @Test
    fun `should return generic customers page when name were given`(){
        every { customerRepository.findByNameLike(any(), any()) } returns PageImpl(listOf(customer))

        val page = customerService.findMany(name=customer.id.toHexString(), pageable = PageRequest.of(0, 10))

        assertEquals(1, page.totalPages)
        assertEquals(1, page.content.size)
        page.content.forEach { assertTrue(it.name.contains(customer.id.toHexString())) }
    }
}