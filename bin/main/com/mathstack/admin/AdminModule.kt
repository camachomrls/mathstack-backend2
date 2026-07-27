package com.mathstack.admin

import com.mathstack.admin.application.GenerateAvatarUseCase
import com.mathstack.admin.application.ListAllUsersUseCase
import com.mathstack.admin.application.UpdateUserCoinsUseCase
import org.koin.dsl.module

val adminModule = module {
    factory { ListAllUsersUseCase(get()) }
    factory { GenerateAvatarUseCase() }
    factory { UpdateUserCoinsUseCase(get()) }
    factory { com.mathstack.admin.application.GetUserProfileUseCase(get()) }
    factory { com.mathstack.admin.application.ListAllDiagnosticsUseCase(get()) }
    factory { com.mathstack.admin.application.ListAllPracticeSessionsUseCase(get()) }
    factory { com.mathstack.admin.application.GetDashboardStatsUseCase(get(), get(), get(), get()) }
    factory { com.mathstack.admin.application.ListAllLessonsUseCase(get()) }
    single<com.mathstack.admin.domain.repository.AdminChallengeRepository> { com.mathstack.admin.infrastructure.persistence.PostgresAdminChallengeRepository() }
    factory { com.mathstack.admin.application.ListAllExercisesUseCase(get()) }
    factory { com.mathstack.admin.application.ListAllChallengesUseCase(get(), get()) }
    factory { com.mathstack.admin.application.CreateAdminChallengeUseCase(get()) }
    factory { com.mathstack.admin.application.UpdateAdminChallengeUseCase(get()) }
    factory { com.mathstack.admin.application.DeleteAdminChallengeUseCase(get()) }
    single<com.mathstack.admin.domain.repository.AdminSettingsRepository> { com.mathstack.admin.infrastructure.persistence.PostgresAdminSettingsRepository() }
    single<com.mathstack.shared.infrastructure.email.EmailService> { com.mathstack.shared.infrastructure.email.SmtpEmailService() }
    factory { com.mathstack.admin.application.GetAdminSettingsUseCase(get()) }
    factory { com.mathstack.admin.application.UpdateAdminSettingsUseCase(get()) }
}
