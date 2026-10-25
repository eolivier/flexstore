package org.flexstore.infra.spring.config

import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases, each one a bean that receives its repositories (see RepositoryConfig and the adapters)
@Configuration
open class UseCaseConfig(@Value("\${flexstore.front-url}") private val frontUrl: String) {

    @Bean
    open fun createProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
    }

    @Bean
    open fun updateProductUseCase(productRepository: ProductRepository): UpdateProductUseCase {
        return UpdateProductUseCase(productRepository)
    }

    @Bean
    open fun readProductUseCase(productRepository: ProductRepository): ReadProductUseCase {
        return ReadProductUseCase(productRepository)
    }

    @Bean
    open fun addItemToCartUseCase(cartRepository: CartRepository,
                                  productRepository: ProductRepository): AddItemToCartUseCase {
        return AddItemToCartUseCase(cartRepository, productRepository)
    }

    @Bean
    open fun changeCartItemQuantityUseCase(cartRepository: CartRepository): ChangeCartItemQuantityUseCase {
        return ChangeCartItemQuantityUseCase(cartRepository)
    }

    @Bean
    open fun readCartUseCase(cartRepository: CartRepository): ReadCartUseCase {
        return ReadCartUseCase(cartRepository)
    }

    @Bean
    open fun mergeCartsUseCase(cartRepository: CartRepository): MergeCartsUseCase {
        return MergeCartsUseCase(cartRepository)
    }

    @Bean
    open fun startPaymentUseCase(orderRepository: OrderRepository,
                                 paymentRepository: PaymentRepository,
                                 userRepository: UserRepository) =
        StartPaymentUseCase(orderRepository, paymentRepository, userRepository)

    @Bean
    open fun confirmPaymentUseCase(orderRepository: OrderRepository, cartRepository: CartRepository) =
        ConfirmPaymentUseCase(orderRepository, cartRepository)

    @Bean
    open fun readPaymentUseCase(paymentRepository: PaymentRepository) = ReadPaymentUseCase(paymentRepository)

    @Bean
    open fun paymentUseCases(startPaymentUseCase: StartPaymentUseCase,
                             confirmPaymentUseCase: ConfirmPaymentUseCase,
                             readPaymentUseCase: ReadPaymentUseCase) =
        PaymentUseCases(startPaymentUseCase, confirmPaymentUseCase, readPaymentUseCase)

    @Bean
    open fun paidOrderUseCases(readOrdersUseCase: ReadOrdersUseCase,
                               sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase) =
        PaidOrderUseCases(readOrdersUseCase, sendOrderConfirmationEmailUseCase)

    @Bean
    open fun sendOrderConfirmationEmailUseCase(shopSettingsRepository: ShopSettingsRepository,
                                               userRepository: UserRepository,
                                               emailRepository: EmailRepository) =
        SendOrderConfirmationEmailUseCase(shopSettingsRepository, userRepository, emailRepository)

    @Bean
    open fun readShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository): ReadShopSettingsUseCase {
        return ReadShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun updateShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository):
        UpdateShopSettingsUseCase {
        return UpdateShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun placeOrderUseCase(cartRepository: CartRepository, orderRepository: OrderRepository,
                               userRepository: UserRepository): PlaceOrderUseCase {
        return PlaceOrderUseCase(cartRepository, orderRepository, userRepository)
    }

    @Bean
    open fun readOrdersUseCase(orderRepository: OrderRepository): ReadOrdersUseCase {
        return ReadOrdersUseCase(orderRepository)
    }

    @Bean
    open fun advanceOrderStatusUseCase(orderRepository: OrderRepository): AdvanceOrderStatusUseCase {
        return AdvanceOrderStatusUseCase(orderRepository)
    }

    @Bean
    open fun changePostalAddressUseCase(userRepository: UserRepository) =
        ChangePostalAddressUseCase(userRepository)

    @Bean
    open fun removePostalAddressUseCase(userRepository: UserRepository) =
        RemovePostalAddressUseCase(userRepository)

    @Bean
    open fun addressUseCases(changePostalAddressUseCase: ChangePostalAddressUseCase,
                             removePostalAddressUseCase: RemovePostalAddressUseCase) =
        AddressUseCases(changePostalAddressUseCase, removePostalAddressUseCase)

    @Bean
    open fun registerCustomerUseCase(userRepository: UserRepository): RegisterCustomerUseCase {
        return RegisterCustomerUseCase(userRepository)
    }

    @Bean
    // The links of the verification emails open the page /verify-email of the front
    open fun sendVerificationEmailUseCase(userRepository: UserRepository,
                                          emailRepository: EmailRepository): SendVerificationEmailUseCase {
        val verificationPage = VerificationPage("${frontUrl.trimEnd('/')}/verify-email")
        return SendVerificationEmailUseCase(userRepository, emailRepository, verificationPage)
    }

    @Bean
    open fun verifyEmailUseCase(userRepository: UserRepository,
                                emailRepository: EmailRepository): VerifyEmailUseCase {
        return VerifyEmailUseCase(userRepository, emailRepository)
    }

    @Bean
    open fun readAccountUseCase(userRepository: UserRepository): ReadAccountUseCase {
        return ReadAccountUseCase(userRepository)
    }
}
