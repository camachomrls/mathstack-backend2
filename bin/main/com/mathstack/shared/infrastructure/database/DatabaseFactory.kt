package com.mathstack.shared.infrastructure.database

import com.mathstack.academic.infrastructure.persistence.ExerciseTable
import com.mathstack.academic.infrastructure.persistence.LessonTable
import com.mathstack.social.infrastructure.persistence.FriendsTable
import com.mathstack.social.infrastructure.persistence.ChallengesTable
import com.mathstack.social.infrastructure.persistence.ChallengeParticipantsTable
import com.mathstack.social.infrastructure.persistence.GroupsTable
import com.mathstack.social.infrastructure.persistence.GroupMembersTable
import com.mathstack.academic.infrastructure.persistence.LessonTypeTable
import com.mathstack.academic.infrastructure.persistence.SubjectTable
import com.mathstack.practice.infrastructure.persistence.DiagnosticResultsTable
import com.mathstack.practice.infrastructure.persistence.ExerciseAttemptsTable
import com.mathstack.practice.infrastructure.persistence.LearningPathsTable
import com.mathstack.practice.infrastructure.persistence.PracticeSessionsTable
import com.mathstack.admin.infrastructure.persistence.AdminChallengesTable
import com.mathstack.admin.infrastructure.persistence.AdminSettingsTable
import com.mathstack.store.infrastructure.persistence.ItemTypeTable
import com.mathstack.store.infrastructure.persistence.StoreItemTable
import com.mathstack.store.infrastructure.persistence.UserInventoryTable
import com.mathstack.users.infrastructure.persistence.UserGamificationStatsTable
import com.mathstack.users.infrastructure.persistence.UserTable
import com.mathstack.users.infrastructure.persistence.UserProficiencyTable
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import com.mathstack.auth.infrastructure.persistence.OtpCodeTable

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        Database.connect(hikari(config))
        
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UserTable,
                UserGamificationStatsTable,
                SubjectTable,
                LessonTypeTable,
                LessonTable,
                ExerciseTable,
                ItemTypeTable,
                StoreItemTable,
                UserInventoryTable,
                DiagnosticResultsTable,
                LearningPathsTable,
                PracticeSessionsTable,
                ExerciseAttemptsTable,
                FriendsTable,
                ChallengesTable,
                ChallengeParticipantsTable,
                AdminChallengesTable,
                AdminSettingsTable,
                GroupsTable,
                GroupMembersTable,
                UserProficiencyTable,
                OtpCodeTable
            )

            val defaultSubjects = listOf(
                "Aritmética",
                "Álgebra",
                "Cálculo Integral",
                "Cálculo Diferencial",
                "Cálculo de Varias Variables",
                "Ecuaciones Diferenciales"
            )
            
            val existingSubjects = SubjectTable.selectAll().map { it[SubjectTable.name] }
            
            defaultSubjects.forEach { subjectName ->
                if (!existingSubjects.contains(subjectName)) {
                    SubjectTable.insert {
                        it[name] = subjectName
                    }
                }
            }

            val existingLessonTypes = LessonTypeTable.selectAll().map { it[LessonTypeTable.name] }
            if (!existingLessonTypes.contains("Estándar")) {
                LessonTypeTable.insert {
                    it[name] = "Estándar"
                }
            }
        }
    }

    private fun hikari(config: ApplicationConfig): HikariDataSource {
        return HikariDataSource().apply {
            var rawUrl = com.mathstack.shared.infrastructure.config.Env.get("DB_URL") ?: config.propertyOrNull("database.url")?.getString() ?: "jdbc:postgresql://localhost:5432/mathstack"
            
            var userFromUrl: String? = null
            var passFromUrl: String? = null
            
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                val uri = java.net.URI(rawUrl)
                if (uri.userInfo != null) {
                    val split = uri.userInfo.split(":")
                    userFromUrl = split[0]
                    passFromUrl = if (split.size > 1) split[1] else null
                }
                rawUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
            } else if (!rawUrl.startsWith("jdbc:")) {
                rawUrl = "jdbc:$rawUrl"
            }
            
            jdbcUrl = rawUrl
            username = userFromUrl ?: com.mathstack.shared.infrastructure.config.Env.get("DB_USER") ?: config.propertyOrNull("database.user")?.getString() ?: "postgres"
            password = passFromUrl ?: com.mathstack.shared.infrastructure.config.Env.get("DB_PASSWORD") ?: config.propertyOrNull("database.password")?.getString() ?: "postgres"
            driverClassName = com.mathstack.shared.infrastructure.config.Env.get("DB_DRIVER") ?: config.propertyOrNull("database.driver")?.getString() ?: "org.postgresql.Driver"
            
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 30000
            connectionTimeout = 2000
            maxLifetime = 1800000
        }
    }
}