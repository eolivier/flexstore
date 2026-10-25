package org.flexstore.infra.spring.config

import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The repositories that are not components: the adapters of the ports (@Repository) are found by the scan.
// The PostgreSQL user adapter is @Primary: this in-memory one is not used by the application
@Configuration
open class RepositoryConfig {

    @Bean
    open fun userRepository(): UserRepository {
        return InMemoryUserRepository()
    }
}
