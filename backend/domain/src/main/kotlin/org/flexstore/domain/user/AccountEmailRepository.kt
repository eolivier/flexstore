package org.flexstore.domain.user

// Sends the emails about the accounts; the infrastructure decides how (SMTP, logs...)
interface AccountEmailRepository {
    fun send(email: EmailVerificationEmail)
}
