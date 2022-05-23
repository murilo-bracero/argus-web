package br.com.argus.authapi.controller

import br.com.argus.authapi.dto.LoginRequestDTO
import br.com.argus.authapi.dto.LoginResponseDTO
import br.com.argus.authapi.dto.UserResponseDTO
import br.com.argus.authapi.exception.AuthNeedsMfaException
import br.com.argus.authapi.model.AuthResultEnum
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.service.CustomerService
import br.com.argus.authapi.service.UserAthenticationService
import br.com.argus.authapi.utils.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class LoginController(
    @Autowired private val userAthenticationService: UserAthenticationService,
    @Autowired private val customerService: CustomerService,
    @Autowired private val jwtUtils: JwtUtils
) {

    @PostMapping
    fun login(@RequestBody @Valid request: LoginRequestDTO): LoginResponseDTO {
        when (userAthenticationService.login(request.email, request.password, request.mfaCode, request.userSystem)) {
            AuthResultEnum.AUTHENTICATED -> {

                var user: UserResponseDTO

                if (request.userSystem == SystemEnum.CUSTOMER) {
                    user = customerService.findByEmail(request.email);
                } else {
                    TODO()
                }

                return LoginResponseDTO(user, jwtUtils.generateToken(user.id, request.userSystem))
            }
            AuthResultEnum.NEED_MFA -> throw AuthNeedsMfaException()
            AuthResultEnum.MFA_DENIED -> throw BadCredentialsException("mfa code is invalid")
            AuthResultEnum.LOGIN_DENIED -> throw BadCredentialsException("credentials are invalid")
        }
    }

}