package br.com.argus.authapi.utils

import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.Tokens
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtils(
    @Value("\${security.jwt.secret}")
    private val secret: String,

    @Value("\${security.jwt.expiration}")
    private val expiration: Int,

    @Value("\${security.jwt.refresh_expiration}")
    private val refreshExpiration: Int

) {

    fun generateToken(userId: String, system: SystemEnum): Tokens{
        val algorithm = Algorithm.HMAC512(secret)

        val builder = JWT.create()
            .withSubject(userId)
            .withClaim("system", system.toString())

        return Tokens(
            builder.withExpiresAt(Date(System.currentTimeMillis() + expiration)).sign(algorithm),
            builder.withExpiresAt(Date(System.currentTimeMillis() + refreshExpiration)).sign(algorithm),
        )
    }

    fun verify(token: String): Map<String, Claim> {
        TODO()
    }
}