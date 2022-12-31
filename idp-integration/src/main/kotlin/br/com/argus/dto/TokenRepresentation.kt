package br.com.argus.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenRepresentation(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Int
) {
}