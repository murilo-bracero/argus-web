package br.com.argus.dto

data class UserRepresentation(
    val email: String,
    val isEnabled: Boolean,
    val credentials: List<CredentialsRepresentation>
)
