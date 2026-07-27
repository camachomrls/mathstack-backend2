package com.mathstack.auth.domain.repository

import com.mathstack.auth.domain.model.OtpCode
import java.util.UUID

interface OtpCodeRepository {
    fun save(otpCode: OtpCode)
    fun findByUserId(userId: UUID): OtpCode?
    fun deleteByUserId(userId: UUID)
}
