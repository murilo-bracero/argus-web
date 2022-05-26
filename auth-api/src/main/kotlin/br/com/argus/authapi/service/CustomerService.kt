package br.com.argus.authapi.service

import br.com.argus.authapi.dto.CustomerResponseDTO

interface CustomerService {

    fun findByEmail(email: String): CustomerResponseDTO

}