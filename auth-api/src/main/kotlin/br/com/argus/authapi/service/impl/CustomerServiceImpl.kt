package br.com.argus.authapi.service.impl

import br.com.argus.authapi.dto.CustomerResponseDTO
import br.com.argus.authapi.service.CustomerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class CustomerServiceImpl(
    @Value("\${service-urls.customer-service}")
    private val url: String,

    @Autowired private val restTemplate: RestTemplate
) : CustomerService {
    override fun findByEmail(email: String): CustomerResponseDTO {
        return restTemplate.getForObject("$url?email=$email")
    }
}