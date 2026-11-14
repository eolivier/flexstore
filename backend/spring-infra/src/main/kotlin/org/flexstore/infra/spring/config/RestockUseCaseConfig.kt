package org.flexstore.infra.spring.config

import org.flexstore.domain.email.EmailRepository
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.restock.RestockSubscriptionRepository
import org.flexstore.domain.restock.usecase.*
import org.flexstore.domain.stock.StockRepository
import org.flexstore.domain.user.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the restock sub-domain, declared step by step: use cases, then their group
@Configuration
open class RestockUseCaseConfig {

    @Bean
    open fun subscribeToRestockUseCase(
        restockSubscriptionRepository: RestockSubscriptionRepository,
        productRepository: ProductRepository,
        stockRepository: StockRepository
    ) = SubscribeToRestockUseCase(restockSubscriptionRepository, productRepository, stockRepository)

    @Bean
    open fun listCustomerRestockSubscriptionsUseCase(restockSubscriptionRepository: RestockSubscriptionRepository) =
        ListCustomerRestockSubscriptionsUseCase(restockSubscriptionRepository)

    @Bean
    open fun listRestockedSubscriptionsUseCase(
        restockSubscriptionRepository: RestockSubscriptionRepository,
        stockRepository: StockRepository
    ) = ListRestockedSubscriptionsUseCase(restockSubscriptionRepository, stockRepository)

    @Bean
    open fun notifyRestockUseCase(
        restockSubscriptionRepository: RestockSubscriptionRepository,
        productRepository: ProductRepository,
        stockRepository: StockRepository,
        userRepository: UserRepository,
        emailRepository: EmailRepository
    ) = NotifyRestockUseCase(
        restockSubscriptionRepository, productRepository, stockRepository, userRepository, emailRepository
    )

    @Bean
    open fun restockUseCases(
        subscribeToRestockUseCase: SubscribeToRestockUseCase,
        listCustomerRestockSubscriptionsUseCase: ListCustomerRestockSubscriptionsUseCase,
        listRestockedSubscriptionsUseCase: ListRestockedSubscriptionsUseCase,
        notifyRestockUseCase: NotifyRestockUseCase
    ) = RestockUseCases(
        subscribeToRestockUseCase, listCustomerRestockSubscriptionsUseCase, listRestockedSubscriptionsUseCase,
        notifyRestockUseCase
    )
}
