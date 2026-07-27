package com.mathstack.auth.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mathstack.auth.domain.repository.TokenService
import io.ktor.server.config.ApplicationConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

class JwtTokenService(
    config: ApplicationConfig,
) : TokenService {
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val secret = config.property("jwt.secret").getString()
    private val expiresInHours = config.propertyOrNull("jwt.expiresInHours")?.getString()?.toLong() ?: 24L

    override fun generate(userId: UUID, email: String, accessLevel: String): String {
        val expiresAt = Instant.now().plus(expiresInHours, ChronoUnit.HOURS)

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("user_id", userId.toString())
            .withClaim("email", email)
            .withClaim("access_level", accessLevel)
            .withExpiresAt(Date.from(expiresAt))
            .sign(Algorithm.HMAC256(secret))
    }

    override fun generateTempToken(userId: UUID, email: String): String {
        val expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES)

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("user_id", userId.toString())
            .withClaim("email", email)
            .withClaim("temp_otp", true)
            .withExpiresAt(Date.from(expiresAt))
            .sign(Algorithm.HMAC256(secret))
    }
}
