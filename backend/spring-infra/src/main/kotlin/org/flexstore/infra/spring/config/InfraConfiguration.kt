package org.flexstore.infra.spring.config

import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class InfraConfiguration {

    @Bean
    open fun userService(userRepository: UserRepository): UserService {
        return UserService(userRepository)
    }

    @Bean
    open fun userRepository(): UserRepository {
        return InMemoryUserRepository()
    }
}