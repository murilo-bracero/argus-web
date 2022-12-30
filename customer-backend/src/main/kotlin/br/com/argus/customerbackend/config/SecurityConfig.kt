package br.com.argus.customerbackend.config

import jakarta.servlet.http.HttpServletResponse
import br.com.argus.customerbackend.services.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired private val authenticationService: AuthenticationService
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .cors().disable()
            .sessionManagement { sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { eh -> eh.authenticationEntryPoint { req, res, ex -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED) } }
            .authorizeHttpRequests { ar ->
                ar.requestMatchers(HttpMethod.POST, "/customers").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth -> oauth.jwt() }

        return http.build();
    }
}