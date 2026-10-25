package org.flexstore.infra.spring.adapter.payment

import com.stripe.exception.SignatureVerificationException
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import org.flexstore.domain.order.OrderId

// The notifications of Stripe: only those signed with the secret of the webhook are trusted
class StripeWebhook(private val webhookSecret: String) {

    // The order whose payment the notification confirms, if it confirms one
    fun paidOrderOf(payload: String, signature: String): OrderId? {
        val event = try {
            Webhook.constructEvent(payload, signature, webhookSecret)
        } catch (exception: SignatureVerificationException) {
            throw InvalidStripeSignatureException(exception)
        }
        if (event.type != "checkout.session.completed") return null
        val session = event.dataObjectDeserializer.deserializeUnsafe() as Session
        return if (session.paymentStatus == "paid") OrderId(session.clientReferenceId) else null
    }
}

class InvalidStripeSignatureException(cause: Throwable) : RuntimeException("Invalid Stripe signature", cause)
