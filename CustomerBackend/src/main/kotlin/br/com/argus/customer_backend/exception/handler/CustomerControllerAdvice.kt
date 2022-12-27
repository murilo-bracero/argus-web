package br.com.argus.customer_backend.exception.handler

import br.com.argus.customer_backend.dto.ErrorResponse
import br.com.argus.customer_backend.exception.CustomerException
import com.mongodb.MongoWriteException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*
import kotlin.NoSuchElementException

@RestControllerAdvice
class CustomerControllerAdvice {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ErrorResponse {
        val message = ex.bindingResult.fieldErrors.joinToString(";") {
            "${it.field}: ${it.defaultMessage}"
        }

        return ErrorResponse("001", message)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleRuntimeException(ex: Exception): ErrorResponse {
        logger.error{"An unknown error occurred: $ex"}

        return ErrorResponse("099", "An error occurred while processing your request, please try again later")
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [(IllegalArgumentException::class)])
    fun handleIllegalArgumentException(): ErrorResponse {
        return ErrorResponse("004", "Provided parameter is invalid or malformed")
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [(InvalidPropertiesFormatException::class)])
    fun handleInvalidPropertiesFormatException(ex: InvalidPropertiesFormatException): ErrorResponse {
        return ErrorResponse("001", ex.message.orEmpty())
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = [(NoSuchElementException::class)])
    fun handleNoSuchElementException(ex: NoSuchElementException): ErrorResponse {
        return ErrorResponse("003", "customer with provided ${ex.message} does not exists")
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = [(MongoWriteException::class)])
    fun handleException(): ErrorResponse {
        return ErrorResponse("002", "customer already exists")
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [(CustomerException::class)])
    fun handleCustomerException(customerException: CustomerException): ErrorResponse {
        logger.error { "Customer exception occurred: ${customerException.cause}" }
        return ErrorResponse(customerException.id, customerException.message)
    }

}