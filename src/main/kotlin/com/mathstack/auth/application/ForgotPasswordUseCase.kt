package com.mathstack.auth.application

import com.mathstack.auth.domain.model.OtpCode
import com.mathstack.auth.domain.model.OtpPolicy
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.shared.domain.email.EmailSender
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime

class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
    private val otpCodeRepository: OtpCodeRepository,
    private val passwordHasher: PasswordHasher,
    private val emailSender: EmailSender,
) {
    suspend operator fun invoke(command: ForgotPasswordCommand) {
        val user = userRepository.findUserByEmail(command.email)
            ?: return 

        val code = AuthSecretGenerator.otp()
        val createdAt = LocalDateTime.now()
        val expiresAt = createdAt.plusMinutes(OtpPolicy.PASSWORD_RESET_TTL_MINUTES)
        otpCodeRepository.saveOtp(OtpCode(user.email, passwordHasher.hash(code), createdAt, expiresAt))

        emailSender.sendEmail(
            user.email,
            AuthEmailTemplates.PASSWORD_RESET_OTP_SUBJECT,
            AuthEmailTemplates.passwordResetOtp(code),
        )
    }
}

data class ForgotPasswordCommand(
    val email: String,
)
