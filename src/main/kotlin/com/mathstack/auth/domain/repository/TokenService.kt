package com.mathstack.auth.domain.repository

import java.util.UUID

interface TokenService {
    fun generate(userId: UUID, email: String, accessLevel: String): String
    fun generateTempToken(userId: UUID, email: String): String
}
