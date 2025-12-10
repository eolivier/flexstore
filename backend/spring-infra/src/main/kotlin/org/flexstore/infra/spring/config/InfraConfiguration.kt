package org.flexstore.infra.spring.config

import org.flexstore.domain.port.PasswordEncoder
import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.service.UserService
import org.flexstore.infra.repository.InMemoryItemRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class InfraConfiguration {

    @Bean
    open fun userService(userRepository: UserRepository, passwordEncoder: PasswordEncoder): UserService {
        return UserService(userRepository, passwordEncoder)
    }

    @Bean
    open fun userRepository(): UserRepository {
        return InMemoryUserRepository()
    }

    @Bean
    open fun itemRepository(): ItemRepository {
        return InMemoryItemRepository()
    }

    @Bean
    open fun productsRepository(): ProductRepository {
        return InMemoryProductRepository()
    }
}