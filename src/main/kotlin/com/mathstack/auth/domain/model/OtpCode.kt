package com.mathstack.auth.domain.model

import java.time.LocalDateTime

data class OtpCode(
    val email: String,
    val codeHash: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
)
