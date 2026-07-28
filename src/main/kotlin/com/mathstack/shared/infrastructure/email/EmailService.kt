package com.mathstack.shared.infrastructure.email

import com.mathstack.shared.domain.email.EmailSender

@Deprecated("Use the EmailSender port from the domain layer.")
typealias EmailService = EmailSender
