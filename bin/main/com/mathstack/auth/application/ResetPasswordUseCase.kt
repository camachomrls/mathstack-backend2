package com.mathstack.auth.application

import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.shared.domain.exception.UnauthorizedException
import com.mathstack.shared.infrastructure.email.EmailService
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime

class ResetPasswordUseCase(
    private val userRepository: UserRepository,
    private val otpCodeRepository: OtpCodeRepository,
    private val passwordHasher: PasswordHasher,
    private val emailService: EmailService
) {
    operator fun invoke(command: ResetPasswordCommand) {
        val user = userRepository.findUserByEmail(command.email)
            ?: throw UnauthorizedException("Invalid email or code")

        val otp = otpCodeRepository.findByUserId(user.id)
            ?: throw UnauthorizedException("Invalid or expired code")

        if (otp.code != command.code || otp.expiresAt.isBefore(LocalDateTime.now())) {
            throw UnauthorizedException("Invalid or expired code")
        }

        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val newPassword = (1..10).map { chars.random() }.joinToString("")
        val passwordHash = passwordHasher.hash(newPassword)

        userRepository.updatePassword(user.id, passwordHash)
        otpCodeRepository.deleteByUserId(user.id)

        emailService.sendEmail(
            user.email,
            "Tu nueva contraseña de MathStack",
            "<p>Tu contraseña ha sido restablecida exitosamente. Tu nueva contraseña es: <strong>$newPassword</strong></p><p>Te recomendamos cambiarla una vez que inicies sesión.</p>"
        )
    }
}

data class ResetPasswordCommand(
    val email: String,
    val code: String,
)
