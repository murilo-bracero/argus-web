package br.com.argus.authapi.config

import br.com.argus.authapi.filters.JwtTokenFilter
import br.com.argus.authapi.service.CredentialsService
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
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
class WebSecurityAdapterConfig(
    @Autowired private val credentialsService: CredentialsService,
    @Autowired private val jwtUtils: JwtUtils
) : WebSecurityConfigurerAdapter() {

    @Profile("local")
    @Throws(Exception::class)
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

        http.authorizeRequests().anyRequest().permitAll()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}