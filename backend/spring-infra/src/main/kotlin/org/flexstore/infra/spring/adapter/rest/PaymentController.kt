package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.order.OrderId
import org.flexstore.domain.payment.OrderPayment
import org.flexstore.domain.payment.usecase.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.payment.InvalidStripeSignatureException
import org.flexstore.infra.spring.adapter.payment.StripeWebhook
import org.flexstore.infra.spring.adapter.rest.json.JsonPaymentSession
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@Tag(name = "Payments")
@RestController
class PaymentController(
    private val paymentService: PaymentService,
    private val stripeWebhook: ObjectProvider<StripeWebhook>
) {

    @Operation(summary = "Start the payment of an order of the signed-in customer, then open its checkout page")
    @PostMapping("/api/orders/{orderId}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    fun pay(@AuthenticationPrincipal jwt: Jwt, @PathVariable orderId: String): JsonPaymentSession {
        val customer = CartOwner.Customer(UserId.ValidUserId(jwt.subject))
        val paymentSession = paymentService.pay(OrderPayment(customer, OrderId(orderId)))
        return JsonPaymentSession.of(paymentSession)
    }

    // Called by Stripe, not by the front: the signature of the notification authenticates it
    @Operation(summary = "Notification of Stripe: a checkout session is completed")
    @PostMapping("/api/payments/stripe/webhook")
    fun stripeNotification(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<Void> {
        val webhook = stripeWebhook.ifAvailable ?: return ResponseEntity.notFound().build()
        val paidOrder = try {
            webhook.paidOrderOf(payload, signature)
        } catch (exception: InvalidStripeSignatureException) {
            return ResponseEntity.badRequest().build()
        }
        paidOrder?.let { paymentService.confirm(it) }
        return ResponseEntity.ok().build()
    }
}
