package br.com.argus.authapi.config

import br.com.argus.authapi.filters.JwtAuthenticationFilter
import br.com.argus.authapi.service.CredentialsService
import br.com.argus.authapi.service.UserAuthenticationService
import br.com.argus.authapi.utils.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
class WebSecurityAdapterConfig(
    @Autowired private val jwtUtils: JwtUtils,
    @Autowired private val credentialsService: CredentialsService,
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable()

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.exceptionHandling()
            .authenticationEntryPoint { req, res, ex ->
                res.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ex.message
                )
            }

        http.authorizeRequests().antMatchers(HttpMethod.POST, "/auth").permitAll()
        http.authorizeRequests().antMatchers(HttpMethod.GET, "/auth/refresh").permitAll()
        http.authorizeRequests().anyRequest().authenticated()

        http.addFilterBefore(JwtAuthenticationFilter(jwtUtils, credentialsService, listOf("/auth", "/auth/refresh")), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}