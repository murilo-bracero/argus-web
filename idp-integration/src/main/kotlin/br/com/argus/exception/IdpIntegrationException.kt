package br.com.argus.exception

class IdpIntegrationException(
    val id: String,
    message: String,
    cause: Throwable?
): Exception(message, cause) {

    constructor(id: String, message: String): this(id, message, null)

}