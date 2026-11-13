package org.flexstore.infra.spring.config

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the payment sub-domain, declared step by step: use cases, then their group
@Configuration
open class PaymentUseCaseConfig {

    @Bean
    open fun startPaymentUseCase(orderRepository: OrderRepository,
                                 paymentRepository: PaymentRepository,
                                 userRepository: UserRepository) =
        StartPaymentUseCase(orderRepository, paymentRepository, userRepository)

    @Bean
    open fun confirmPaymentUseCase(orderRepository: OrderRepository,
                                   cartRepository: CartRepository,
                                   stockRepository: StockRepository) =
        ConfirmPaymentUseCase(orderRepository, cartRepository, stockRepository)

    @Bean
    open fun readPaymentUseCase(paymentRepository: PaymentRepository) = ReadPaymentUseCase(paymentRepository)

    @Bean
    open fun verifyPaymentUseCase(orderRepository: OrderRepository,
                                  paymentRepository: PaymentRepository,
                                  userRepository: UserRepository) =
        VerifyPaymentUseCase(orderRepository, paymentRepository, userRepository)

    @Bean
    open fun paymentUseCases(startPaymentUseCase: StartPaymentUseCase,
                             confirmPaymentUseCase: ConfirmPaymentUseCase,
                             readPaymentUseCase: ReadPaymentUseCase,
                             verifyPaymentUseCase: VerifyPaymentUseCase) =
        PaymentUseCases(startPaymentUseCase, confirmPaymentUseCase, readPaymentUseCase, verifyPaymentUseCase)
}
