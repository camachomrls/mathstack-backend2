package com.mathstack.auth.application

import com.mathstack.auth.domain.model.AuthSession
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.shared.domain.exception.UnauthorizedException
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime

class VerifyOtpUseCase(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val otpCodeRepository: OtpCodeRepository,
) {
    operator fun invoke(command: VerifyOtpCommand): AuthSession {
        val user = userRepository.findUserByEmail(command.email)
            ?: throw UnauthorizedException("Invalid email or code")

        val otp = otpCodeRepository.findByUserId(user.id)
            ?: throw UnauthorizedException("Invalid or expired code")

        if (otp.code != command.code || otp.expiresAt.isBefore(LocalDateTime.now())) {
            throw UnauthorizedException("Invalid or expired code")
        }

        otpCodeRepository.deleteByUserId(user.id)

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
