package org.flexstore.infra.spring.config

import org.flexstore.domain.repository.CartRepository
import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.usecase.basket.AddItemToCartUseCase
import org.flexstore.domain.usecase.basket.CartUseCaseFacade
import org.flexstore.domain.usecase.basket.ChangeCartItemQuantityUseCase
import org.flexstore.domain.usecase.basket.MergeCartsUseCase
import org.flexstore.domain.usecase.basket.ReadCartUseCase
import org.flexstore.domain.order.OrderUseCaseFacade
import org.flexstore.domain.order.PlaceOrderUseCase
import org.flexstore.domain.order.ReadOrdersUseCase
import org.flexstore.domain.usecase.product.CreateProductUseCase
import org.flexstore.domain.usecase.product.ProductUseCaseFacade
import org.flexstore.domain.usecase.product.ReadProductUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UseCaseConfig {

    @Bean
    open fun productUseCaseFacade(createProductUseCase: CreateProductUseCase,
                                 readProductUseCase: ReadProductUseCase): ProductUseCaseFacade {
        return ProductUseCaseFacade(createProductUseCase, readProductUseCase)
    }

    @Bean
    open fun createProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
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
    open fun orderUseCaseFacade(placeOrderUseCase: PlaceOrderUseCase, readOrdersUseCase: ReadOrdersUseCase): OrderUseCaseFacade {
        return OrderUseCaseFacade(placeOrderUseCase, readOrdersUseCase)
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