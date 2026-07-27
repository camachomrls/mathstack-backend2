package com.mathstack.auth

import com.mathstack.auth.application.LoginUseCase
import com.mathstack.auth.application.RegisterUseCase
import com.mathstack.auth.domain.repository.PasswordHasher
import com.mathstack.auth.domain.repository.TokenService
import com.mathstack.auth.infrastructure.security.BCryptPasswordHasher
import com.mathstack.auth.infrastructure.security.JwtTokenService
import com.mathstack.auth.domain.repository.OtpCodeRepository
import com.mathstack.auth.infrastructure.persistence.PostgresOtpCodeRepository
import com.mathstack.auth.application.VerifyOtpUseCase
import com.mathstack.auth.application.ForgotPasswordUseCase
import com.mathstack.auth.application.ResetPasswordUseCase
import org.koin.dsl.module

val authModule = module {
    single<PasswordHasher> { BCryptPasswordHasher() }
    single<TokenService> { JwtTokenService(get()) }
    single<OtpCodeRepository> { PostgresOtpCodeRepository() }
    single { LoginUseCase(get(), get(), get(), get(), get()) }
    factory { VerifyOtpUseCase(get(), get(), get()) }
    factory { ForgotPasswordUseCase(get(), get(), get()) }
    factory { ResetPasswordUseCase(get(), get(), get(), get()) }
    factory { RegisterUseCase(get(), get(), get()) }
    factory { com.mathstack.auth.application.LoginWithGoogleAndOtpUseCase(get(), get(), get(), get()) }
}
