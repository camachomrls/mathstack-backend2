package com.mathstack.auth.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.UUID

object OtpCodeTable : Table("otp_codes") {
    val userId = uuid("user_id").uniqueIndex()
    val code = varchar("code", 10)
    val expiresAt = datetime("expires_at")
}
