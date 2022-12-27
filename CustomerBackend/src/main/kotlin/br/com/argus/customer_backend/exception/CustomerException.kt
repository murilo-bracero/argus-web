package br.com.argus.customer_backend.exception

class CustomerException(
    val id: String,
    override val message: String,
    override val cause: Throwable?
) : Exception(message, cause) {

    constructor(id: String, message: String): this(id, message, null)

}