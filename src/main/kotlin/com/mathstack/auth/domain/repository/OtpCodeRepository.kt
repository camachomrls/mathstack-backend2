package com.mathstack.auth.domain.repository

import com.mathstack.auth.domain.model.OtpCode

interface OtpCodeRepository {
    fun saveOtp(otpCode: OtpCode)
    fun getOtpByEmail(email: String): OtpCode?
    fun deleteOtp(email: String)
}
