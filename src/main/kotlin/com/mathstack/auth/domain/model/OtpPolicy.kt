package com.mathstack.auth.domain.model

object OtpPolicy {
    const val CODE_LENGTH = 6
    const val LOGIN_TTL_MINUTES = 5L
    const val PASSWORD_RESET_TTL_MINUTES = 15L
    const val TEMPORARY_PASSWORD_LENGTH = 16
}
