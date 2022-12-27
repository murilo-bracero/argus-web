package br.com.argus.customer_backend.services

import br.com.argus.customer_backend.dto.CreateCredentialsRequest

interface AuthenticationService {

    fun createCredentials(createCredentialsRequest: CreateCredentialsRequest)

    fun deleteCredentials(idpId: String)

}