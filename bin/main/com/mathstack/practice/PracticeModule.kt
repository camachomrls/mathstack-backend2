package com.mathstack.practice

import com.mathstack.practice.application.GetStudentDashboardMetricsUseCase
import com.mathstack.practice.application.LogPracticeSessionUseCase
import com.mathstack.practice.application.RegisterExerciseAttemptUseCase
import com.mathstack.practice.domain.repository.PracticeRepository
import com.mathstack.practice.infrastructure.persistence.PostgresPracticeRepository
import org.koin.dsl.module

val practiceModule = module {
    single<PracticeRepository> { PostgresPracticeRepository() }
    single { RegisterExerciseAttemptUseCase(get(), get(), get()) }
    single { LogPracticeSessionUseCase(get(), get()) }
    single { GetStudentDashboardMetricsUseCase(get()) }
    single { com.mathstack.practice.application.GenerateDiagnosticQuizUseCase(get()) }
    single { com.mathstack.practice.application.SubmitDiagnosticAnswersUseCase(get(), get(), get(), get()) }
    single { com.mathstack.practice.application.GetLearningPathUseCase(get(), get()) }
    single { com.mathstack.practice.application.GetSubjectProgressUseCase(get(), get()) }
}
