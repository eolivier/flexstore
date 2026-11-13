package org.flexstore.infra.spring.config

import org.flexstore.domain.product.*
import org.flexstore.domain.wishlist.*
import org.flexstore.domain.wishlist.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the wishlist sub-domain, declared step by step: use cases, then their group
@Configuration
open class WishlistUseCaseConfig {

    @Bean
    open fun addProductToWishlistUseCase(
        wishlistRepository: WishlistRepository,
        productRepository: ProductRepository
    ) = AddProductToWishlistUseCase(wishlistRepository, productRepository)

    @Bean
    open fun removeProductFromWishlistUseCase(wishlistRepository: WishlistRepository) =
        RemoveProductFromWishlistUseCase(wishlistRepository)

    @Bean
    open fun listWishlistUseCase(wishlistRepository: WishlistRepository) =
        ListWishlistUseCase(wishlistRepository)

    @Bean
    open fun wishlistUseCases(
        addProductToWishlistUseCase: AddProductToWishlistUseCase,
        removeProductFromWishlistUseCase: RemoveProductFromWishlistUseCase,
        listWishlistUseCase: ListWishlistUseCase
    ) = WishlistUseCases(addProductToWishlistUseCase, removeProductFromWishlistUseCase, listWishlistUseCase)
}
