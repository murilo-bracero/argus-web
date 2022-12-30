package br.com.argus.customerbackend.services

import br.com.argus.customerbackend.dto.CreateCredentialsRequest

interface AuthenticationService {

    fun createCredentials(createCredentialsRequest: CreateCredentialsRequest)

    fun deleteCredentials(idpId: String)

}