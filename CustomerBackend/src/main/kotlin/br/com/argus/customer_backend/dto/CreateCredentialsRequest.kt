package br.com.argus.customer_backend.dto

class CreateCredentialsRequest(
    val userId: String,
    val email: String,
    val password: String
) {

}