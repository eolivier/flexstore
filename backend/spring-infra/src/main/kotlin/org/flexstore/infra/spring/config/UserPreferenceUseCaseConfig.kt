package org.flexstore.infra.spring.config

import org.flexstore.domain.preference.UserPreferenceRepository
import org.flexstore.domain.preference.usecase.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the user preference sub-domain, declared step by step: use cases, then their group
@Configuration
open class UserPreferenceUseCaseConfig {

    @Bean
    open fun readUserPreferenceUseCase(
        userPreferenceRepository: UserPreferenceRepository,
        userRepository: UserRepository
    ) = ReadUserPreferenceUseCase(userPreferenceRepository, userRepository)

    @Bean
    open fun recordUserPreferenceUseCase(
        userPreferenceRepository: UserPreferenceRepository,
        userRepository: UserRepository
    ) = RecordUserPreferenceUseCase(userPreferenceRepository, userRepository)

    @Bean
    open fun userPreferenceUseCases(
        readUserPreferenceUseCase: ReadUserPreferenceUseCase,
        recordUserPreferenceUseCase: RecordUserPreferenceUseCase
    ) = UserPreferenceUseCases(readUserPreferenceUseCase, recordUserPreferenceUseCase)
}
