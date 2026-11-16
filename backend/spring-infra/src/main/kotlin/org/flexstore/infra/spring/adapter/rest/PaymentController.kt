package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.payment.OrderPayment
import org.flexstore.domain.payment.PaymentService
import org.flexstore.infra.spring.adapter.payment.InvalidStripeSignatureException
import org.flexstore.infra.spring.adapter.payment.StripeWebhook
import org.flexstore.infra.spring.adapter.rest.json.JsonPaymentSession
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
class PaymentController(
    private val paymentService: PaymentService,
    private val stripeWebhook: ObjectProvider<StripeWebhook>,
    private val actorResolutionService: ActorResolutionService
) {

    @PostMapping("/api/orders/{orderId}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    fun pay(@AuthenticationPrincipal jwt: Jwt, @PathVariable orderId: String): JsonPaymentSession {
        val customer = actorResolutionService.actorOf(jwt)
        val paymentSession = paymentService.pay(OrderPayment(customer, OrderId(Identity(orderId))))
        return JsonPaymentSession.of(paymentSession)
    }

    // The customer comes back from the payment page: the order is paid if the provider says so, even when its
    // notification did not arrive
    @PostMapping("/api/orders/{orderId}/payment/verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@AuthenticationPrincipal jwt: Jwt, @PathVariable orderId: String) {
        val customer = actorResolutionService.actorOf(jwt)
        paymentService.verify(OrderPayment(customer, OrderId(Identity(orderId))))
    }

    // Called by Stripe, not by the front: the signature of the notification authenticates it
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
