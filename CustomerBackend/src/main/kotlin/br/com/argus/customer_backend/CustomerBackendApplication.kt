package br.com.argus.customer_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerBackendApplication

fun main(args: Array<String>) {

	runApplication<CustomerBackendApplication>(*args)
}
