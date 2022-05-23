package br.com.argus.authapi.exception.handler

import br.com.argus.authapi.dto.ErrorResponseDTO
import br.com.argus.authapi.exception.AuthNeedsMfaException
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllersAdvice {

    private val logger = KotlinLogging.logger {  }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ErrorResponseDTO{
        val message = ex.bindingResult.fieldErrors.joinToString(";") {
            "${it.field}: ${it.defaultMessage}"
        }

        return ErrorResponseDTO("001", message)
    }

    @ExceptionHandler(value = [BadCredentialsException::class])
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentialsException(ex: BadCredentialsException): ErrorResponseDTO {
        return ErrorResponseDTO("098", ex.message.orEmpty())
    }

    @ExceptionHandler(value = [AuthNeedsMfaException::class])
    fun handleAuthNeedsMfaException(): ResponseEntity<ErrorResponseDTO> {

        val headers = HttpHeaders()
        headers.add("WWW-Authenticate", "authType=\"TOTP\"")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .headers(headers)
            .body(ErrorResponseDTO("098", "MFA needed to login"))
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleRuntimeException(ex: Exception): ErrorResponseDTO {
        logger.error{"An unknown error occurred: $ex"}

        return ErrorResponseDTO("099", "An error occurred while processing your request, please try again later")
    }

}