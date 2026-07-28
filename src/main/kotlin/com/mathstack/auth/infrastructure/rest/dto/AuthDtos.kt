package com.mathstack.auth.infrastructure.rest.dto

import com.mathstack.auth.application.LoginCommand
import com.mathstack.auth.application.RegisterCommand
import com.mathstack.auth.application.VerifyOtpCommand
import com.mathstack.auth.application.ForgotPasswordCommand
import com.mathstack.auth.application.ResetPasswordCommand
import com.mathstack.auth.domain.model.AuthSession
import com.mathstack.auth.domain.model.OtpPolicy
import com.mathstack.shared.domain.exception.ValidationException
import com.mathstack.users.infrastructure.rest.dto.UserResponse
import com.mathstack.users.infrastructure.rest.dto.toResponse
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val firebaseUid: String? = null,
    val accessLevel: String = "STUDENT",
)

@Serializable
data class LoginWithGoogleRequest(
    val email: String,
    val username: String,
    val firebaseUid: String? = null
)

fun LoginWithGoogleRequest.toCommand(): com.mathstack.auth.application.LoginWithGoogleCommand {
    validateEmail(email)
    validateUsername(username)
    if (firebaseUid != null && firebaseUid.isBlank()) {
        throw ValidationException("firebaseUid must not be blank")
    }

    return com.mathstack.auth.application.LoginWithGoogleCommand(
        email = email.normalizedEmail(),
        username = username.trim(),
        firebaseUid = firebaseUid?.trim(),
    )
}

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val code: String,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
)

fun VerifyOtpRequest.toCommand(): VerifyOtpCommand =
    VerifyOtpCommand(email = email.validatedEmail(), code = code.validatedOtpCode())

fun ForgotPasswordRequest.toCommand(): ForgotPasswordCommand =
    ForgotPasswordCommand(email = email.validatedEmail())

fun ResetPasswordRequest.toCommand(): ResetPasswordCommand =
    ResetPasswordCommand(email = email.validatedEmail(), code = code.validatedOtpCode())

@Serializable
data class AuthResponse(
    val token: String? = null,
    val user: UserResponse? = null,
    val requiresOtp: Boolean = false,
    val tempToken: String? = null,
    val email: String? = null,
)

fun LoginRequest.toCommand(): LoginCommand {
    validateEmail(email)
    if (password.isBlank()) {
        throw ValidationException("password is required")
    }
    return LoginCommand(email = email.normalizedEmail(), password = password)
}

fun RegisterRequest.toCommand(): RegisterCommand {
    validateEmail(email)
    validateUsername(username)
    validateStrongPassword(password)
    val normalizedAccessLevel = accessLevel.trim().uppercase()
    if (normalizedAccessLevel !in setOf("STUDENT", "TEACHER", "ADMIN")) {
        throw ValidationException("accessLevel must be STUDENT, TEACHER or ADMIN")
    }
    if (firebaseUid != null && firebaseUid.isBlank()) {
        throw ValidationException("firebaseUid must not be blank")
    }

    return RegisterCommand(
        email = email.normalizedEmail(),
        username = username.trim(),
        password = password,
        firebaseUid = firebaseUid?.trim(),
        accessLevel = normalizedAccessLevel,
    )
}

fun AuthSession.toResponse(): AuthResponse =
    AuthResponse(
        token = token,
        user = user?.toResponse(),
        requiresOtp = requiresOtp,
        tempToken = tempToken,
        email = user?.email
    )

private fun validateEmail(value: String) {
    if (!EmailRegex.matches(value.normalizedEmail())) {
        throw ValidationException("email must be valid")
    }
}

private fun String.validatedEmail(): String {
    validateEmail(this)
    return normalizedEmail()
}

private fun String.validatedOtpCode(): String {
    val normalizedCode = trim()
    if (!OtpCodeRegex.matches(normalizedCode)) {
        throw ValidationException("code must be a ${OtpPolicy.CODE_LENGTH}-digit OTP")
    }
    return normalizedCode
}

private fun validateUsername(value: String) {
    if (value.trim().length !in 3..50) {
        throw ValidationException("username must contain between 3 and 50 characters")
    }
}

private fun validateStrongPassword(value: String) {
    if (value.length < 8) {
        throw ValidationException("password must contain at least 8 characters")
    }
    if (!value.any(Char::isUpperCase) || !value.any(Char::isLowerCase) || !value.any(Char::isDigit)) {
        throw ValidationException("password must contain uppercase, lowercase and numeric characters")
    }
    if (value.none { !it.isLetterOrDigit() }) {
        throw ValidationException("password must contain at least one special character")
    }
}

private fun String.normalizedEmail(): String = trim().lowercase()

private val EmailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val OtpCodeRegex = Regex("^\\d{${OtpPolicy.CODE_LENGTH}}$")
