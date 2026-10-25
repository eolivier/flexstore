package org.flexstore.infra.spring.adapter.payment

import com.stripe.net.Webhook
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.order.OrderId
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StripeWebhookTest : WithAssertions {

    private val webhookSecret = "whsec_test_secret"
    private val stripeWebhook = StripeWebhook(webhookSecret)

    private fun notification(type: String, paymentStatus: String) = """
        {"id": "evt_1", "object": "event", "api_version": "2024-12-18.acacia", "type": "$type",
         "data": {"object": {"id": "cs_1", "object": "checkout.session",
                             "client_reference_id": "order-1", "payment_status": "$paymentStatus"}}}
    """.trimIndent()

    // The header Stripe sends: a timestamp and the HMAC-SHA256 of "timestamp.payload" with the secret
    private fun signatureOf(payload: String, secret: String = webhookSecret): String {
        val timestamp = Webhook.Util.getTimeNow()
        val signature = Webhook.Util.computeHmacSha256(secret, "$timestamp.$payload")
        return "t=$timestamp,v1=$signature"
    }

    @Test
    fun `a completed and paid checkout should confirm the payment of its order`() {
        // Arrange
        val payload = notification("checkout.session.completed", "paid")
        // Act
        val paidOrder = stripeWebhook.paidOrderOf(payload, signatureOf(payload))
        // Assert
        assertThat(paidOrder).isEqualTo(OrderId("order-1"))
    }

    @ParameterizedTest(name = "a notification {0} with a payment {1} should confirm nothing")
    @CsvSource("checkout.session.completed, unpaid", "checkout.session.expired, unpaid")
    fun `another notification should confirm no payment`(type: String, paymentStatus: String) {
        // Arrange
        val payload = notification(type, paymentStatus)
        // Act
        val paidOrder = stripeWebhook.paidOrderOf(payload, signatureOf(payload))
        // Assert
        assertThat(paidOrder).isNull()
    }

    @Test
    fun `a notification not signed with the secret of the webhook should be refused`() {
        // Arrange
        val payload = notification("checkout.session.completed", "paid")
        val forgedSignature = signatureOf(payload, secret = "whsec_another_secret")
        // Act
        val thrown = catchThrowable { stripeWebhook.paidOrderOf(payload, forgedSignature) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidStripeSignatureException::class.java)
    }
}
