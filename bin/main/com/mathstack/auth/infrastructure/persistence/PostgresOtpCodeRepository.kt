package com.mathstack.auth.infrastructure.persistence

import com.mathstack.auth.domain.model.OtpCode
import com.mathstack.auth.domain.repository.OtpCodeRepository
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresOtpCodeRepository : OtpCodeRepository {
    override fun save(otpCode: OtpCode) {
        transaction {
            OtpCodeTable.deleteWhere { OtpCodeTable.userId eq otpCode.userId }
            OtpCodeTable.insert {
                it[userId] = otpCode.userId
                it[code] = otpCode.code
                it[expiresAt] = otpCode.expiresAt
            }
        }
    }

    override fun findByUserId(userId: UUID): OtpCode? {
        return transaction {
            OtpCodeTable.selectAll().where { OtpCodeTable.userId eq userId }
                .map {
                    OtpCode(
                        userId = it[OtpCodeTable.userId],
                        code = it[OtpCodeTable.code],
                        expiresAt = it[OtpCodeTable.expiresAt]
                    )
                }
                .singleOrNull()
        }
    }

    override fun deleteByUserId(userId: UUID) {
        transaction {
            OtpCodeTable.deleteWhere { OtpCodeTable.userId eq userId }
        }
    }
}
