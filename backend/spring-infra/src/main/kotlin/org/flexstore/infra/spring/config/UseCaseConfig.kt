package org.flexstore.infra.spring.config

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UseCaseConfig {

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
    open fun shopSettingsUseCaseFacade(shopSettingsRepository: ShopSettingsRepository): ShopSettingsUseCaseFacade {
        return ShopSettingsUseCaseFacade(shopSettingsRepository, UpdateShopSettingsUseCase(shopSettingsRepository))
    }

    @Bean
    open fun placeOrderUseCase(cartRepository: CartRepository, orderRepository: OrderRepository): PlaceOrderUseCase {
        return PlaceOrderUseCase(cartRepository, orderRepository)
    }

    @Bean
    open fun readOrdersUseCase(orderRepository: OrderRepository): ReadOrdersUseCase {
        return ReadOrdersUseCase(orderRepository)
    }
}