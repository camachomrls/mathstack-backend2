package com.mathstack.auth.application

import com.mathstack.auth.domain.model.AuthSession
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.shared.domain.exception.UnauthorizedException
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime

class VerifyOtpUseCase(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val otpCodeRepository: OtpCodeRepository,
    private val passwordHasher: PasswordHasher,
) {
    operator fun invoke(command: VerifyOtpCommand): AuthSession {
        val user = userRepository.findUserByEmail(command.email)
            ?: throw UnauthorizedException("Invalid email or code")

        val otp = otpCodeRepository.getOtpByEmail(command.email)
            ?: throw UnauthorizedException("Invalid or expired code")

        if (!otp.expiresAt.isAfter(LocalDateTime.now()) || !passwordHasher.verify(command.code, otp.codeHash)) {
            throw UnauthorizedException("Invalid or expired code")
        }

        otpCodeRepository.deleteOtp(command.email)

        return AuthSession(
            token = tokenService.generate(user.id, user.email, user.accessLevel),
            user = user,
        )
    }
}

data class VerifyOtpCommand(
    val email: String,
    val code: String,
)
