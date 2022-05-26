package br.com.argus.customer_backend.filter

import br.com.argus.customer_backend.services.AuthenticationService
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
    private val authenticationService: AuthenticationService,
    private val bypassPaths: List<String>
): OncePerRequestFilter() {

    private val bearer = "Bearer"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val creds = Optional.ofNullable(request.getHeader(AUTHORIZATION))
            .map { it.removePrefix(bearer) }
            .map { it.trim() }
            .map { authenticationService.authenticate(it) }
            .orElseThrow{ BadCredentialsException("token headers is missing or malformed") }

        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(creds, null, Collections.emptyList())

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return bypassPaths.map { it.split(":") }
            .any { request.method == it[0] && request.servletPath.equals(it[1]) }
    }
}