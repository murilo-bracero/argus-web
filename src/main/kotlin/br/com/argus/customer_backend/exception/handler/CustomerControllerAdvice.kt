package br.com.argus.customer_backend.exception.handler

import br.com.argus.customer_backend.dto.ErrorResponseDTO
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomerControllerAdvice {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ErrorResponseDTO {
        val message = ex.bindingResult.fieldErrors.joinToString(";") {
            "${it.field}: ${it.defaultMessage}"
        }

        return ErrorResponseDTO("001", message)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleRuntimeException(ex: Exception): ErrorResponseDTO {
        logger.error{"An unknown error occurred: $ex"}

        return ErrorResponseDTO("099", "An error occurred while processing your request, please try again later")
    }


}