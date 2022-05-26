package br.com.argus.customer_backend.config

import br.com.argus.customer_backend.filter.JwtAuthenticationFilter
import br.com.argus.customer_backend.services.AuthenticationService
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
    @Autowired private val authenticationService: AuthenticationService
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        http.cors().disable()

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.exceptionHandling()
            .authenticationEntryPoint { req, res, ex ->
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.message)
            }

        http.authorizeRequests().antMatchers(HttpMethod.POST, "/customers").permitAll()
        http.authorizeRequests().anyRequest().authenticated()

        http.addFilterBefore(JwtAuthenticationFilter(authenticationService, listOf("POST:/customers")), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

}