package br.com.argus.customer_backend

import br.com.argus.customer_backend.config.IdpProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(IdpProperties::class)
class CustomerBackendApplication

fun main(args: Array<String>) {

	runApplication<CustomerBackendApplication>(*args)
}
