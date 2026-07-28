package com.mathstack.auth.application

import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.shared.domain.email.EmailSender
import com.mathstack.shared.domain.exception.UnauthorizedException
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime

class ResetPasswordUseCase(
    private val userRepository: UserRepository,
    private val otpCodeRepository: OtpCodeRepository,
    private val passwordHasher: PasswordHasher,
    private val emailSender: EmailSender,
) {
    suspend operator fun invoke(command: ResetPasswordCommand) {
        val user = userRepository.findUserByEmail(command.email)
            ?: throw UnauthorizedException("Invalid email or code")

        val otp = otpCodeRepository.getOtpByEmail(command.email)
            ?: throw UnauthorizedException("Invalid or expired code")

        if (!otp.expiresAt.isAfter(LocalDateTime.now()) || !passwordHasher.verify(command.code, otp.codeHash)) {
            throw UnauthorizedException("Invalid or expired code")
        }

        val newPassword = AuthSecretGenerator.temporaryPassword()
        val passwordHash = passwordHasher.hash(newPassword)

        userRepository.updatePassword(user.id, passwordHash)
        otpCodeRepository.deleteOtp(command.email)

        emailSender.sendEmail(
            user.email,
            AuthEmailTemplates.TEMPORARY_PASSWORD_SUBJECT,
            AuthEmailTemplates.temporaryPassword(newPassword),
        )
    }
}

data class ResetPasswordCommand(
    val email: String,
    val code: String,
)
