package br.com.argus.authapi.filters

import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.utils.JwtUtils
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
    private val jwtUtils: JwtUtils,
    private val credentialsService: CredentialsService,
    private val bypassPath: List<String>
) : OncePerRequestFilter(){

    private val bearer = "Bearer"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val decoded = Optional.ofNullable(request.getHeader(AUTHORIZATION))
            .map { it.removePrefix(bearer) }
            .map { it.trim() }
            .map { jwtUtils.verify(it) }
            .orElseThrow{ BadCredentialsException("Missing Authorization Token") }

        val id = Optional.ofNullable(decoded["id"]).orElseThrow{ BadCredentialsException("Malformed Authorization Token") }
        val system = Optional.ofNullable(decoded["system"])
            .map { SystemEnum.valueOf(it) }
            .orElseThrow{ BadCredentialsException("Malformed Authorization Token") }

        val credentials = credentialsService.find(id, system)

        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(credentials, null, Collections.emptyList())
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return bypassPath.any { request.servletPath.equals(it) }
    }
}