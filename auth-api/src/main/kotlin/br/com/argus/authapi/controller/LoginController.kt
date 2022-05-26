package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.LoginRequestDTO
import br.com.argus.authapi.dto.LoginResponseDTO
import br.com.argus.authapi.dto.UserResponseDTO
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.Tokens
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.service.CustomerService
import br.com.argus.authapi.service.UserAuthenticationService
import br.com.argus.authapi.utils.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class LoginController(
    @Autowired private val credentialsService: CredentialsService,
    @Autowired private val userAuthenticationService: UserAuthenticationService,
    @Autowired private val customerService: CustomerService,
    @Autowired private val jwtUtils: JwtUtils
) {

    @PostMapping
    fun login(@RequestBody @Valid request: LoginRequestDTO): LoginResponseDTO {
        val user = userAuthenticationService.login(request.email, request.password, request.mfaCode, request.userSystem)

        var userResponse: UserResponseDTO

        if (request.userSystem == SystemEnum.CUSTOMER) {
            userResponse = customerService.findByEmail(request.email);
        } else {
            TODO()
        }

        val tokens = jwtUtils.generateToken(userResponse.id, request.userSystem)

        user.credentials.refreshToken = tokens.refreshToken

        credentialsService.update(user.credentials)

        return LoginResponseDTO(userResponse, tokens)
    }

    @GetMapping("/refresh")
    fun refresh(@RequestParam refreshToken: String): Tokens {
        val decoded = jwtUtils.verify(refreshToken);

        val id = Optional.ofNullable(decoded["id"]).orElseThrow{BadCredentialsException("Invalid or malformed token")}
        val system = Optional.ofNullable(decoded["system"])
            .map { SystemEnum.valueOf(it) }
            .orElseThrow{BadCredentialsException("Invalid or malformed token")}

        val creds = credentialsService.find(id, system)

        if(creds.refreshToken != refreshToken){
            throw BadCredentialsException("Invalid token")
        }

        val tokens = jwtUtils.generateToken(id, system)

        creds.refreshToken = tokens.refreshToken

        credentialsService.update(creds)

        return tokens
    }

}