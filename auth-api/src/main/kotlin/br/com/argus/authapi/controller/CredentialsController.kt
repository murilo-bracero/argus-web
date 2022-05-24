package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.CreateUserCredentialsDTO
import br.com.argus.authapi.dto.ErrorResponseDTO
import br.com.argus.authapi.dto.UpdateUserCredentialsDTO
import br.com.argus.authapi.dto.UpdateUserCredentialsResponseDTO
import br.com.argus.authapi.model.UserCredential
import br.com.argus.authapi.service.CredentialsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@RequestMapping("/credentials")
class CredentialsController(
    @Autowired private val credentialsService: CredentialsService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createCredentials(@RequestBody @Valid request: CreateUserCredentialsDTO) {

        credentialsService.createCredential(UserCredential.from(request))
    }

    @PatchMapping
    fun updateCredentials(
        @RequestBody @Valid request: UpdateUserCredentialsDTO
    ): UpdateUserCredentialsResponseDTO {
        val principal = SecurityContextHolder.getContext().authentication.principal as UserCredential

        val creds = credentialsService.find(userId = principal.userId, system = principal.system)

        request.fill(creds)

        val newSecret = credentialsService.update(creds)

        if(newSecret != null) {
            return UpdateUserCredentialsResponseDTO(newSecret)
        }

        return UpdateUserCredentialsResponseDTO()
    }

    @ExceptionHandler(value = [NoSuchElementException::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleMethodArgumentNotValidException(): ErrorResponseDTO {
        return ErrorResponseDTO("002", "user id does not exists")
    }
}