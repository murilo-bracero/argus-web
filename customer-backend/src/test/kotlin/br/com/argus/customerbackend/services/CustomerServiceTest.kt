package br.com.argus.customerbackend.services

import br.com.argus.customerbackend.dto.CreateCustomerRequest
import br.com.argus.customerbackend.models.Customer
import br.com.argus.customerbackend.repositories.CustomerRepository
import br.com.argus.customerbackend.services.impl.CustomerServiceImpl
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.NoSuchElementException

@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var authenticationService: AuthenticationService

    @InjectMockKs
    private lateinit var customerService: CustomerServiceImpl

    private lateinit var request: CreateCustomerRequest

    private lateinit var customer: Customer

    @BeforeEach
    fun setUp(){
        val id = ObjectId.get()

        request = CreateCustomerRequest(
            "52637405066",
            id.toHexString(),
            "${id.toHexString()}@mail.com",
            id.toHexString(),
            "",
            "85741548754"
        )

        customer = request.toModel()
    }

    @Test
    fun `should create new user`(){
        val mockCustomer = request.toModel()
        every { customerRepository.save(any()) } returns mockCustomer
        every { authenticationService.createCredentials(any()) } returns Unit

        val savedCustomer = customerService.save(request)

        assertEquals(mockCustomer.id, savedCustomer.id)

        verify(exactly = 1) { authenticationService.createCredentials(any()) }
    }

    @Test
    fun `should retrieve user when valid cpf were given`(){
        every { customerRepository.findByCpf(any()) } returns Optional.of(customer)

        val savedCustomer = customerService.findByCpf(customer.cpf)

        assertEquals(customer.id, savedCustomer.id)
        assertEquals(customer.cpf, savedCustomer.cpf)

    }

    @Test
    fun `should throw NoSuchElementException when no customer with provided cpf were found`(){
        every { customerRepository.findByCpf(any()) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            customerService.findByCpf("54196584512")
        }
    }

    @Test
    fun `should retrieve user when valid id were given`(){
        every { customerRepository.findById(any()) } returns Optional.of(customer)

        val savedCustomer = customerService.findById(customer.id.toHexString())

        assertEquals(customer.id, savedCustomer.id)
    }

    @Test
    fun `should throw NoSuchElementException when no customer with provided id were found`(){
        every { customerRepository.findById(any()) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            customerService.findById(customer.id.toHexString())
        }
    }

    @Test
    fun `should remove user with provided id`(){
        every { customerRepository.findById(any()) } returns Optional.of(customer)
        every { customerRepository.deleteById(any()) } returns Unit
        every { authenticationService.deleteCredentials(any()) } returns Unit

        customerService.delete(id=customer.id)

        verify(exactly = 1) { customerRepository.deleteById(any()) }
    }

    @Test
    fun `should return generic customers page when no name were given`(){
        every { customerRepository.findAll(any() as Pageable) } returns PageImpl(listOf(customer))

        val page = customerService.findAll(PageRequest.of(0, 10))

        assertEquals(1, page.totalPages)
        assertEquals(1, page.content.size)
    }
}