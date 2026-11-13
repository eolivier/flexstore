package org.flexstore.infra.spring.config

import org.flexstore.domain.user.repository.*
import org.flexstore.domain.user.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the refresh session sub-domain, declared step by step: use cases, then their group
@Configuration
open class RefreshSessionUseCaseConfig {

    @Bean
    open fun openSessionUseCase(
        refreshSessionRepository: RefreshSessionRepository,
        userRepository: UserRepository
    ) = OpenSessionUseCase(refreshSessionRepository, userRepository)

    @Bean
    open fun readRefreshSessionUseCase(refreshSessionRepository: RefreshSessionRepository) =
        ReadRefreshSessionUseCase(refreshSessionRepository)

    @Bean
    open fun rotateRefreshSessionUseCase(
        refreshSessionRepository: RefreshSessionRepository,
        userRepository: UserRepository
    ) = RotateRefreshSessionUseCase(refreshSessionRepository, userRepository)

    @Bean
    open fun revokeRefreshSessionUseCase(refreshSessionRepository: RefreshSessionRepository) =
        RevokeRefreshSessionUseCase(refreshSessionRepository)

    @Bean
    open fun revokeAllRefreshSessionsUseCase(refreshSessionRepository: RefreshSessionRepository) =
        RevokeAllRefreshSessionsUseCase(refreshSessionRepository)
}
