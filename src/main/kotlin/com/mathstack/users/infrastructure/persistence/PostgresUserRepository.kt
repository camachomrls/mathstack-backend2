package com.mathstack.users.infrastructure.persistence

import com.mathstack.users.domain.model.User
import com.mathstack.users.domain.model.UserGamificationStats
import com.mathstack.users.domain.model.UserProfile
import com.mathstack.users.domain.repository.UserRepository
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class PostgresUserRepository : UserRepository {
    override fun createUser(user: User): User =
        transaction {
            UserTable.insert {
                it[id] = user.id
                it[firebaseUid] = user.firebaseUid
                it[email] = user.email
                it[username] = user.username
                it[passwordHash] = user.passwordHash
                it[accessLevel] = user.accessLevel
                it[createdAt] = user.createdAt
            }

            if (findStatsByUserIdInTransaction(user.id) == null) {
                insertStatsInTransaction(UserGamificationStats(userId = user.id))
            }

            findUserByIdInTransaction(user.id)!!
        }

    override fun findUserById(id: UUID): User? =
        transaction {
            findUserByIdInTransaction(id)
        }

    override fun findUserByFirebaseUid(firebaseUid: String): User? =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.firebaseUid eq firebaseUid }
                .singleOrNull()
                ?.toUser()
        }

    override fun findUserByEmail(email: String): User? =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.email eq email }
                .singleOrNull()
                ?.toUser()
        }

    override fun findUserByUsername(username: String): User? =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.username eq username }
                .singleOrNull()
                ?.toUser()
        }

    override fun findAll(): List<User> =
        transaction {
            UserTable.selectAll().map { it.toUser() }
        }

    override fun findAllProfiles(): List<UserProfile> =
        transaction {
            (UserTable leftJoin UserGamificationStatsTable)
                .selectAll()
                .map { row ->
                    val user = row.toUser()
                    val stats = row.toUserGamificationStatsOrNull() 
                        ?: UserGamificationStats(userId = user.id)
                    UserProfile(user, stats)
                }
        }

    override fun existsById(id: UUID): Boolean =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.id eq id }
                .limit(1)
                .count() > 0
        }

    override fun updateUser(user: User): User? =
        transaction {
            val updatedRows = UserTable.update(where = { UserTable.id eq user.id }) {
                it[email] = user.email
                it[username] = user.username
                it[accessLevel] = user.accessLevel
            }

            if (updatedRows == 0) {
                null
            } else {
                findUserByIdInTransaction(user.id)
            }
        }

    override fun deleteUser(id: UUID): Boolean =
        transaction {
            deleteStatsInTransaction(id)
            UserTable.deleteWhere { UserTable.id eq id } > 0
        }

    override fun updatePassword(userId: UUID, passwordHash: String): Boolean =
        transaction {
            UserTable.update({ UserTable.id eq userId }) {
                it[this.passwordHash] = passwordHash
            } > 0
        }

    override fun createStats(stats: UserGamificationStats): UserGamificationStats =
        transaction {
            insertStatsInTransaction(stats)
        }

    override fun findStatsByUserId(userId: UUID): UserGamificationStats? =
        transaction {
            findStatsByUserIdInTransaction(userId)
        }

    override fun updateStats(stats: UserGamificationStats): UserGamificationStats? =
        transaction {
            val updatedRows = UserGamificationStatsTable.update(
                where = { UserGamificationStatsTable.userId eq stats.userId },
            ) {
                it[coins] = stats.coins
                it[currentLevel] = stats.currentLevel
                it[xpPoints] = stats.xpPoints
                it[lessonsCompletedCount] = stats.lessonsCompletedCount
                it[currentStreak] = stats.currentStreak
                it[maxStreak] = stats.maxStreak
                it[minutesPracticed] = stats.minutesPracticed
                it[lastPracticeDate] = stats.lastPracticeDate
                it[lastDiagnosticDate] = stats.lastDiagnosticDate
            }

            if (updatedRows == 0) {
                null
            } else {
                findStatsByUserIdInTransaction(stats.userId)
            }
        }

    override fun deleteStats(userId: UUID): Boolean =
        transaction {
            deleteStatsInTransaction(userId)
        }

    override fun findProfileByUserId(userId: UUID): UserProfile? =
        transaction {
            val user = findUserByIdInTransaction(userId) ?: return@transaction null
            val stats = findStatsByUserIdInTransaction(userId)
                ?: insertStatsInTransaction(UserGamificationStats(userId = userId))

            UserProfile(user = user, gamificationStats = stats)
        }

    override fun getLeaderboard(limit: Int): List<UserProfile> =
        transaction {
            (UserTable innerJoin UserGamificationStatsTable)
                .selectAll()
                .orderBy(UserGamificationStatsTable.xpPoints to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    UserProfile(
                        user = row.toUser(),
                        gamificationStats = row.toUserGamificationStats()
                    )
                }
        }

    private fun findUserByIdInTransaction(id: UUID): User? =
        UserTable
            .selectAll()
            .where { UserTable.id eq id }
            .singleOrNull()
            ?.toUser()

    private fun findStatsByUserIdInTransaction(userId: UUID): UserGamificationStats? =
        UserGamificationStatsTable
            .selectAll()
            .where { UserGamificationStatsTable.userId eq userId }
            .singleOrNull()
            ?.toUserGamificationStats()

    private fun insertStatsInTransaction(stats: UserGamificationStats): UserGamificationStats {
        UserGamificationStatsTable.insert {
            it[userId] = stats.userId
            it[coins] = stats.coins
            it[currentLevel] = stats.currentLevel
            it[xpPoints] = stats.xpPoints
            it[lessonsCompletedCount] = stats.lessonsCompletedCount
            it[currentStreak] = stats.currentStreak
            it[maxStreak] = stats.maxStreak
            it[minutesPracticed] = stats.minutesPracticed
            it[lastPracticeDate] = stats.lastPracticeDate
            it[lastDiagnosticDate] = stats.lastDiagnosticDate
        }

        return findStatsByUserIdInTransaction(stats.userId)!!
    }

    private fun deleteStatsInTransaction(userId: UUID): Boolean =
        UserGamificationStatsTable.deleteWhere {
            UserGamificationStatsTable.userId eq userId
        } > 0
}
