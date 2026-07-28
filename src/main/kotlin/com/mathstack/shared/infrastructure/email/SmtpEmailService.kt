package com.mathstack.shared.infrastructure.email

import com.mathstack.shared.infrastructure.config.Env
import com.mathstack.shared.domain.email.EmailSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.mail.HtmlEmail
import org.slf4j.LoggerFactory

class SmtpEmailService : EmailSender {
    private val logger = LoggerFactory.getLogger(SmtpEmailService::class.java)

    override suspend fun sendEmail(to: String, subject: String, htmlContent: String) {
        val smtpUser = Env.get("SMTP_USER")
        val smtpPass = Env.get("SMTP_PASSWORD")
        
        if (smtpUser.isNullOrBlank() || smtpPass.isNullOrBlank()) {
            logger.warn("SMTP credentials are not configured. Email will not be sent.")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val email = HtmlEmail()
                email.hostName = "smtp.gmail.com"
                email.setSmtpPort(465)
                email.isSSLOnConnect = true
                email.setAuthentication(smtpUser, smtpPass)
                email.setFrom(smtpUser, "MathStack Soporte")
                email.subject = subject
                email.setHtmlMsg(htmlContent)
                email.addTo(to)
                
                email.send()
                logger.info("Email sent successfully to {} with subject '{}'", to, subject)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Failed to send email to {}", to, e)
            }
        }
    }
}
