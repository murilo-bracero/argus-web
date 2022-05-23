package br.com.argus.authapi.config

import br.com.argus.authapi.service.UserAthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenAuthenticationProvider(
    @Autowired private val userAuthenticationService: UserAthenticationService
): AbstractUserDetailsAuthenticationProvider() {

    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        authentication: UsernamePasswordAuthenticationToken?
    ) {}

    override fun retrieveUser(username: String, authentication: UsernamePasswordAuthenticationToken?): UserDetails {
        return Optional.ofNullable(authentication)
            .map { it.credentials.toString() }
            .flatMap { userAuthenticationService.findByToken(it) }
            .orElseThrow{ UsernameNotFoundException("Provided token is invalid or malformed") }
    }
}