package com.mathstack.users.domain.repository

import com.mathstack.users.domain.model.User
import com.mathstack.users.domain.model.UserGamificationStats
import com.mathstack.users.domain.model.UserProfile
import java.util.UUID

interface UserRepository {
    fun createUser(user: User): User
    fun findUserById(id: UUID): User?
    fun findUserByFirebaseUid(firebaseUid: String): User?
    fun findUserByEmail(email: String): User?
    fun findUserByUsername(username: String): User?
    fun findAll(): List<User>
    fun findAllProfiles(): List<UserProfile>
    fun existsById(id: UUID): Boolean
    fun updateUser(user: User): User?
    fun deleteUser(id: UUID): Boolean

    fun createStats(stats: UserGamificationStats): UserGamificationStats
    fun findStatsByUserId(userId: UUID): UserGamificationStats?
    fun updateStats(stats: UserGamificationStats): UserGamificationStats?
    fun deleteStats(userId: UUID): Boolean

    fun findProfileByUserId(userId: UUID): UserProfile?
    fun getLeaderboard(limit: Int): List<UserProfile>
    fun updatePassword(userId: UUID, passwordHash: String): Boolean
}
