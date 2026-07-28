package com.mathstack.auth.application

import com.mathstack.auth.domain.model.OtpCode
import com.mathstack.auth.domain.model.OtpPolicy
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.shared.domain.email.EmailSender
import com.mathstack.users.domain.model.User
import com.mathstack.users.domain.repository.UserRepository
import java.time.LocalDateTime

class LoginWithGoogleAndOtpUseCase(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val otpCodeRepository: OtpCodeRepository,
    private val passwordHasher: PasswordHasher,
    private val emailSender: EmailSender,
) {
    suspend operator fun invoke(command: LoginWithGoogleCommand) {
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

        val code = AuthSecretGenerator.otp()
        val createdAt = LocalDateTime.now()
        val expiresAt = createdAt.plusMinutes(OtpPolicy.LOGIN_TTL_MINUTES)
        otpCodeRepository.saveOtp(OtpCode(user.email, passwordHasher.hash(code), createdAt, expiresAt))
        emailSender.sendEmail(
            user.email,
            AuthEmailTemplates.GOOGLE_LOGIN_OTP_SUBJECT,
            AuthEmailTemplates.googleLoginOtp(code),
        )
    }
}


