package com.mathstack.auth.infrastructure.security

import com.mathstack.auth.domain.repository.PasswordHasher
import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHasher : PasswordHasher {
    override fun hash(rawPassword: String): String =
        BCrypt.hashpw(rawPassword, BCrypt.gensalt(12))

    override fun verify(rawPassword: String, passwordHash: String): Boolean =
        passwordHash.isNotBlank() && runCatching {
            BCrypt.checkpw(rawPassword, passwordHash)
        }.getOrDefault(false)
}
