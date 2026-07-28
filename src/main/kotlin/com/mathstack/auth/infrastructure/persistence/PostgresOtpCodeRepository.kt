package com.mathstack.auth.infrastructure.persistence

import com.mathstack.auth.domain.model.OtpCode
import com.mathstack.auth.domain.repository.OtpCodeRepository
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PostgresOtpCodeRepository : OtpCodeRepository {
    override fun saveOtp(otpCode: OtpCode) {
        transaction {
            OtpCodeTable.deleteWhere { OtpCodeTable.email eq otpCode.email }
            OtpCodeTable.insert {
                it[email] = otpCode.email
                it[code] = otpCode.codeHash
                it[createdAt] = otpCode.createdAt
                it[expiresAt] = otpCode.expiresAt
            }
        }
    }

    override fun getOtpByEmail(email: String): OtpCode? {
        return transaction {
            OtpCodeTable.selectAll().where { OtpCodeTable.email eq email }
                .map {
                    OtpCode(
                        email = it[OtpCodeTable.email],
                        codeHash = it[OtpCodeTable.code],
                        createdAt = it[OtpCodeTable.createdAt],
                        expiresAt = it[OtpCodeTable.expiresAt]
                    )
                }
                .singleOrNull()
        }
    }

    override fun deleteOtp(email: String) {
        transaction {
            OtpCodeTable.deleteWhere { OtpCodeTable.email eq email }
        }
    }
}
