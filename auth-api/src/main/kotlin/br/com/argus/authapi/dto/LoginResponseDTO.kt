package br.com.argus.authapi.dto

import br.com.argus.authapi.model.Tokens

data class LoginResponseDTO(
    val user: UserResponseDTO,
    val tokens: Tokens
)