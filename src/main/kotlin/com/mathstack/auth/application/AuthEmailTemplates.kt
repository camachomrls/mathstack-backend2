package com.mathstack.auth.application

internal object AuthEmailTemplates {
    const val LOGIN_OTP_SUBJECT = "Tu código de verificación"
    const val PASSWORD_RESET_OTP_SUBJECT = "Restablecer tu contraseña"
    const val GOOGLE_LOGIN_OTP_SUBJECT = "Código de verificación para inicio de sesión"
    const val TEMPORARY_PASSWORD_SUBJECT = "Tu nueva contraseña de MathStack"

    fun loginOtp(code: String): String = """
        <p>Tu código de verificación de dos pasos es: <strong>$code</strong></p>
        <p>Este código expira en 5 minutos.</p>
    """.trimIndent()

    fun passwordResetOtp(code: String): String = """
        <p>Has solicitado restablecer tu contraseña. Tu código de verificación es: <strong>$code</strong></p>
        <p>Este código expira en 15 minutos.</p>
    """.trimIndent()

    fun googleLoginOtp(code: String): String = """
        <p>Tu código de verificación es: <strong>$code</strong></p>
        <p>Este código expira en 5 minutos.</p>
    """.trimIndent()

    fun temporaryPassword(password: String): String = """
        <p>Tu contraseña ha sido restablecida exitosamente. Tu nueva contraseña es: <strong>$password</strong></p>
        <p>Te recomendamos cambiarla una vez que inicies sesión.</p>
    """.trimIndent()
}
