package org.flexstore.infra.spring.config

import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.infra.spring.adapter.payment.PaymentProvider
import org.flexstore.infra.spring.adapter.payment.SimulatedPaymentProvider
import org.flexstore.infra.spring.adapter.payment.StripePaymentProvider
import org.flexstore.infra.spring.adapter.payment.StripeWebhook
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// Stripe when FLEXSTORE_STRIPE_SECRET_KEY is set, simulated payments otherwise
@Configuration
open class PaymentConfig(private val shopUrl: ShopUrl) {

    @Bean
    @ConditionalOnExpression("!'\${flexstore.payment.stripe.secret-key:}'.isBlank()")
    open fun stripePaymentProvider(
        @Value("\${flexstore.payment.stripe.secret-key}") secretKey: String
    ): PaymentProvider = StripePaymentProvider(secretKey, shopUrl)

    @Bean
    @ConditionalOnExpression("!'\${flexstore.payment.stripe.secret-key:}'.isBlank()")
    open fun stripeWebhook(
        @Value("\${flexstore.payment.stripe.webhook-secret}") webhookSecret: String
    ): StripeWebhook = StripeWebhook(webhookSecret)

    @Bean
    @ConditionalOnExpression("'\${flexstore.payment.stripe.secret-key:}'.isBlank()")
    open fun simulatedPaymentProvider(): PaymentProvider = SimulatedPaymentProvider(shopUrl)
}
