package br.com.argus.authapi.model

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)