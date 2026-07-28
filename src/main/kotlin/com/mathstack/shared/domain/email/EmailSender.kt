package com.mathstack.shared.domain.email

/**
 * Outbound email port. Application use cases depend on this contract instead of
 * a concrete infrastructure adapter.
 */
interface EmailSender {
    suspend fun sendEmail(to: String, subject: String, htmlContent: String)
}
