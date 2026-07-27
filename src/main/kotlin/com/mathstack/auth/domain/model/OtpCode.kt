package com.mathstack.auth.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class OtpCode(
    val userId: UUID,
    val code: String,
    val expiresAt: LocalDateTime,
)
