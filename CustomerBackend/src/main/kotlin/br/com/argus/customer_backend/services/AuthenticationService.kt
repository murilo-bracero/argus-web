package br.com.argus.customer_backend.services

import br.com.argus.authapi.Credentials

interface AuthenticationService {

    fun authenticate(token: String): Credentials

    fun createCredentials(userId: String)

    fun deleteCredentials(userId: String)

}