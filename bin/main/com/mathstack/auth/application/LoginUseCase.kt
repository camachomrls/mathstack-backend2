package com.mathstack.auth.application

import com.mathstack.auth.domain.model.AuthSession
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.shared.domain.exception.UnauthorizedException
import com.mathstack.users.domain.repository.UserRepository

import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.shared.infrastructure.email.EmailService
import com.mathstack.auth.domain.model.OtpCode
import java.time.LocalDateTime
import kotlin.random.Random

class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val otpCodeRepository: OtpCodeRepository,
    private val emailService: EmailService,
) {
    operator fun invoke(command: LoginCommand): AuthSession {
        val user = userRepository.findUserByEmail(command.email)
            ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordHasher.verify(command.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val code = String.format("%04d", Random.nextInt(10000))
        val expiresAt = LocalDateTime.now().plusMinutes(5)
        otpCodeRepository.save(OtpCode(user.id, code, expiresAt))
        
        emailService.sendEmail(
            user.email,
            "Tu código de verificación",
            "<p>Tu código de verificación de 2 pasos es: <strong>$code</strong></p><p>Este código expira en 5 minutos.</p>"
        )

        return AuthSession(
            requiresOtp = true,
            tempToken = tokenService.generateTempToken(user.id, user.email),
            user = user, 
        )
    }
}

data class LoginCommand(
    val email: String,
    val password: String,
)
