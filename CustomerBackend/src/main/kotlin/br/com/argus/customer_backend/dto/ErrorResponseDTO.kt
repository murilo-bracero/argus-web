package br.com.argus.customer_backend.dto

data class ErrorResponseDTO (
    val code: String,
    val message: String
        ) {
    constructor() : this("", "")
}