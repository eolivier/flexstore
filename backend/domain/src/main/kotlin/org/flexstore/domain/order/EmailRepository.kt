package org.flexstore.domain.order

// Sends the emails of the orders; the infrastructure decides how (SMTP, logs...)
interface EmailRepository {
    fun send(email: OrderConfirmationEmail)
}
