package com.mathstack.auth.application

import com.mathstack.auth.domain.model.OtpCode
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.shared.infrastructure.email.EmailService
import com.mathstack.users.domain.model.User
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime
import kotlin.random.Random

class LoginWithGoogleAndOtpUseCase(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val otpCodeRepository: OtpCodeRepository,
    private val emailService: EmailService
) {
    operator fun invoke(command: LoginWithGoogleCommand) {
        var user = command.firebaseUid?.let { userRepository.findUserByFirebaseUid(it) }
            ?: userRepository.findUserByEmail(command.email)

        if (user == null) {
            val newUser = User(
                id = java.util.UUID.randomUUID(),
                email = command.email.trim().lowercase(),
                username = command.username.trim(),
                passwordHash = "",
                firebaseUid = command.firebaseUid?.trim() ?: "",
                accessLevel = "STUDENT",
                createdAt = LocalDateTime.now()
            )
            user = userRepository.createUser(newUser)
        }

        val code = String.format("%06d", Random.nextInt(1000000))
        val expiresAt = LocalDateTime.now().plusMinutes(15)
        otpCodeRepository.save(OtpCode(user.id, code, expiresAt))
        emailService.sendEmail(
            user.email,
            "Código de verificación para inicio de sesión",
            "<p>Tu código de verificación es: <strong>$code</strong>. Expira en 15 minutos.</p>"
        )
    }
}

data class LoginWithGoogleCommand(
    val email: String,
    val username: String,
    val firebaseUid: String?
)
