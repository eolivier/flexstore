package org.flexstore.infra.spring.config

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.product.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the cart sub-domain, declared step by step: use cases, then their group
@Configuration
open class CartUseCaseConfig {

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
    open fun listAbandonedCartsUseCase(cartRepository: CartRepository) =
        ListAbandonedCartsUseCase(cartRepository)

    @Bean
    open fun remindAbandonedCartUseCase(cartRepository: CartRepository,
                                        userRepository: UserRepository,
                                        emailRepository: EmailRepository) =
        RemindAbandonedCartUseCase(cartRepository, userRepository, emailRepository)

    @Bean
    open fun cartUseCases(addItemToCartUseCase: AddItemToCartUseCase,
                          changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                          readCartUseCase: ReadCartUseCase,
                          mergeCartsUseCase: MergeCartsUseCase) =
        CartUseCases(addItemToCartUseCase, changeCartItemQuantityUseCase, readCartUseCase, mergeCartsUseCase)

    @Bean
    open fun abandonedCartUseCases(listAbandonedCartsUseCase: ListAbandonedCartsUseCase,
                                   remindAbandonedCartUseCase: RemindAbandonedCartUseCase) =
        AbandonedCartUseCases(listAbandonedCartsUseCase, remindAbandonedCartUseCase)
}
