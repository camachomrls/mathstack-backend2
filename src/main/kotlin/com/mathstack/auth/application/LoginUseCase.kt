package com.mathstack.auth.application

import com.mathstack.auth.domain.model.AuthSession
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.auth.domain.model.OtpPolicy
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.shared.domain.email.EmailSender
import com.mathstack.shared.domain.exception.UnauthorizedException
import com.mathstack.users.domain.repository.UserRepository
import com.mathstack.auth.domain.model.OtpCode
import java.time.LocalDateTime

class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val otpCodeRepository: OtpCodeRepository,
    private val emailSender: EmailSender,
) {
    suspend operator fun invoke(command: LoginCommand): AuthSession {
        val user = userRepository.findUserByEmail(command.email)
            ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordHasher.verify(command.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val code = AuthSecretGenerator.otp()
        val createdAt = LocalDateTime.now()
        val expiresAt = createdAt.plusMinutes(OtpPolicy.LOGIN_TTL_MINUTES)
        otpCodeRepository.saveOtp(OtpCode(user.email, passwordHasher.hash(code), createdAt, expiresAt))
        
        emailSender.sendEmail(
            user.email,
            AuthEmailTemplates.LOGIN_OTP_SUBJECT,
            AuthEmailTemplates.loginOtp(code),
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
