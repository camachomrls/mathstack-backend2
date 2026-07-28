package com.mathstack.auth.application

import com.mathstack.auth.domain.model.OtpPolicy
import java.security.SecureRandom

/** Generates authentication secrets with a cryptographically secure PRNG. */
internal object AuthSecretGenerator {
    private val secureRandom = SecureRandom()
    private val otpUpperBound = (1..OtpPolicy.CODE_LENGTH).fold(1) { bound, _ -> bound * 10 }
    private const val TEMPORARY_PASSWORD_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"

    fun otp(): String =
        secureRandom.nextInt(otpUpperBound)
            .toString()
            .padStart(OtpPolicy.CODE_LENGTH, '0')

    fun temporaryPassword(): String = buildString(OtpPolicy.TEMPORARY_PASSWORD_LENGTH) {
        repeat(OtpPolicy.TEMPORARY_PASSWORD_LENGTH) {
            append(TEMPORARY_PASSWORD_ALPHABET[secureRandom.nextInt(TEMPORARY_PASSWORD_ALPHABET.length)])
        }
    }
}
