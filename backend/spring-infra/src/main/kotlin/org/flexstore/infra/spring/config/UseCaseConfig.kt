package org.flexstore.infra.spring.config

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.usecase.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UseCaseConfig(@Value("\${flexstore.front-url}") private val frontUrl: String) {

    @Bean
    open fun productUseCaseFacade(createProductUseCase: CreateProductUseCase,
                                 readProductUseCase: ReadProductUseCase,
                                 updateProductUseCase: UpdateProductUseCase): ProductUseCaseFacade {
        return ProductUseCaseFacade(createProductUseCase, readProductUseCase, updateProductUseCase)
    }

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
    open fun cartUseCaseFacade(addItemToCartUseCase: AddItemToCartUseCase,
                               changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                               readCartUseCase: ReadCartUseCase,
                               mergeCartsUseCase: MergeCartsUseCase): CartUseCaseFacade {
        return CartUseCaseFacade(addItemToCartUseCase, changeCartItemQuantityUseCase, readCartUseCase, mergeCartsUseCase)
    }

    @Bean
    open fun addItemToCartUseCase(cartRepository: CartRepository, productRepository: ProductRepository): AddItemToCartUseCase {
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
    open fun orderUseCaseFacade(placeOrderUseCase: PlaceOrderUseCase,
                                readOrdersUseCase: ReadOrdersUseCase,
                                sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase): OrderUseCaseFacade {
        return OrderUseCaseFacade(placeOrderUseCase, readOrdersUseCase, sendOrderConfirmationEmailUseCase)
    }

    @Bean
    open fun sendOrderConfirmationEmailUseCase(shopSettingsRepository: ShopSettingsRepository,
                                               userRepository: UserRepository,
                                               emailRepository: EmailRepository): SendOrderConfirmationEmailUseCase {
        return SendOrderConfirmationEmailUseCase(shopSettingsRepository, userRepository, emailRepository)
    }

    @Bean
    open fun shopSettingsAdministrationService(readShopSettingsUseCase: ReadShopSettingsUseCase,
                                        updateShopSettingsUseCase: UpdateShopSettingsUseCase): ShopSettingsAdministrationService {
        return ShopSettingsAdministrationService(readShopSettingsUseCase, updateShopSettingsUseCase)
    }

    @Bean
    open fun readShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository): ReadShopSettingsUseCase {
        return ReadShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun updateShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository): UpdateShopSettingsUseCase {
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
    open fun customerRegistrationService(registerCustomerUseCase: RegisterCustomerUseCase,
                                         sendVerificationEmailUseCase: SendVerificationEmailUseCase,
                                         verifyEmailUseCase: VerifyEmailUseCase,
                                         readAccountUseCase: ReadAccountUseCase): CustomerRegistrationService {
        return CustomerRegistrationService(registerCustomerUseCase, sendVerificationEmailUseCase, verifyEmailUseCase, readAccountUseCase)
    }

    @Bean
    open fun registerCustomerUseCase(userRepository: UserRepository): RegisterCustomerUseCase {
        return RegisterCustomerUseCase(userRepository)
    }

    @Bean
    // The links of the verification emails open the page /verify-email of the front
    open fun sendVerificationEmailUseCase(userRepository: UserRepository,
                                          emailRepository: EmailRepository): SendVerificationEmailUseCase {
        return SendVerificationEmailUseCase(userRepository, emailRepository, VerificationPage("${frontUrl.trimEnd('/')}/verify-email"))
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
