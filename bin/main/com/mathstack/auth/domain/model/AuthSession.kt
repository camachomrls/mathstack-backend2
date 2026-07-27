package com.mathstack.auth.domain.model

import com.mathstack.users.domain.model.User

data class AuthSession(
    val token: String? = null,
    val user: User? = null,
    val requiresOtp: Boolean = false,
    val tempToken: String? = null,
)
