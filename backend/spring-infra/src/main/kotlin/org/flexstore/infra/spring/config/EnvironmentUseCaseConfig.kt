package org.flexstore.infra.spring.config

import org.flexstore.domain.environment.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the environment sub-domain, declared step by step: use cases, then their group
@Configuration
open class EnvironmentUseCaseConfig {

    @Bean
    open fun readEnvironmentUseCase(environmentRepository: EnvironmentRepository) =
        ReadEnvironmentUseCase(environmentRepository)
}
