package br.com.argus.customerbackend

import br.com.argus.customerbackend.config.IdpProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(IdpProperties::class)
class CustomerBackendApplication

fun main(args: Array<String>) {

	runApplication<CustomerBackendApplication>(*args)
}
