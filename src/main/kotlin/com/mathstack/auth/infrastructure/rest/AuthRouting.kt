package com.mathstack.auth.infrastructure.rest

import com.mathstack.auth.application.LoginUseCase
import com.mathstack.auth.application.RegisterUseCase
import com.mathstack.auth.infrastructure.rest.dto.LoginRequest
import com.mathstack.auth.infrastructure.rest.dto.LoginWithGoogleRequest
import com.mathstack.auth.infrastructure.rest.dto.RegisterRequest
import com.mathstack.auth.infrastructure.rest.dto.toCommand
import com.mathstack.auth.infrastructure.rest.dto.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

import com.mathstack.auth.application.VerifyOtpUseCase
import com.mathstack.auth.application.VerifyOtpCommand
import com.mathstack.auth.infrastructure.rest.dto.VerifyOtpRequest

fun Route.authRouting() {
    val loginUseCase by inject<LoginUseCase>()
    val registerUseCase by inject<RegisterUseCase>()
    val loginWithGoogleUseCase by inject<com.mathstack.auth.application.LoginWithGoogleUseCase>()
    val loginWithGoogleAndOtpUseCase by inject<com.mathstack.auth.application.LoginWithGoogleAndOtpUseCase>()
    val verifyOtpUseCase by inject<VerifyOtpUseCase>()

    val forgotPasswordUseCase by inject<com.mathstack.auth.application.ForgotPasswordUseCase>()
    val resetPasswordUseCase by inject<com.mathstack.auth.application.ResetPasswordUseCase>()

    route("/api/v1/auth") {
        post("/login") {
            val session = loginUseCase(call.receive<LoginRequest>().toCommand())
            call.respond(HttpStatusCode.OK, session.toResponse())
        }

        post("/verify-otp") {
            val request = call.receive<VerifyOtpRequest>()
            val session = verifyOtpUseCase(VerifyOtpCommand(request.email, request.code))
            call.respond(HttpStatusCode.OK, session.toResponse())
        }

        post("/forgot-password") {
            val request = call.receive<com.mathstack.auth.infrastructure.rest.dto.ForgotPasswordRequest>()
            forgotPasswordUseCase(com.mathstack.auth.application.ForgotPasswordCommand(request.email))
            call.respond(HttpStatusCode.OK, mapOf("message" to "If the email exists, a code was sent"))
        }

        post("/reset-password") {
            val request = call.receive<com.mathstack.auth.infrastructure.rest.dto.ResetPasswordRequest>()
            resetPasswordUseCase(com.mathstack.auth.application.ResetPasswordCommand(request.email, request.code))
            call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset successfully"))
        }

        post("/login-with-google-otp") {
            val request = call.receive<LoginWithGoogleRequest>()
            // use injected use case defined above
            loginWithGoogleAndOtpUseCase(
                com.mathstack.auth.application.LoginWithGoogleCommand(
                    request.email,
                    request.username,
                    request.firebaseUid
                )
            )
            call.respond(HttpStatusCode.OK, mapOf("message" to "Verification code sent to email"))
        }

        post("/register") {
            val session = registerUseCase(call.receive<RegisterRequest>().toCommand())
            call.respond(HttpStatusCode.Created, session.toResponse())
        }
    }
}
