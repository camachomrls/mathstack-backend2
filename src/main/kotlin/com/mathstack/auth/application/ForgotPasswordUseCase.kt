package com.mathstack.auth.application

import com.mathstack.auth.domain.model.OtpCode
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.shared.infrastructure.email.EmailService
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime
import kotlin.random.Random

class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
    private val otpCodeRepository: OtpCodeRepository,
    private val emailService: EmailService
) {
    operator fun invoke(command: ForgotPasswordCommand) {
        val user = userRepository.findUserByEmail(command.email)
            ?: return 

        val code = String.format("%06d", Random.nextInt(1000000))
        val expiresAt = LocalDateTime.now().plusMinutes(15)
        otpCodeRepository.save(OtpCode(user.id, code, expiresAt))

        emailService.sendEmail(
            user.email,
            "Restablecer tu contraseña",
            "<p>Has solicitado restablecer tu contraseña. Tu código de verificación es: <strong>$code</strong></p><p>Este código expira en 15 minutos.</p>"
        )
    }
}

data class ForgotPasswordCommand(
    val email: String,
)
