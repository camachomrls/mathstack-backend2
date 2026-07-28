package com.mathstack.auth.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object OtpCodeTable : Table("otp_codes") {
    val email = varchar("email", 255).uniqueIndex()
    val code = varchar("code", 60)
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")
}
