package org.flexstore.infra.spring.config

import org.flexstore.domain.twofactor.usecase.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the two factor sub-domain, declared step by step: use cases, then their group
@Configuration
open class TwoFactorUseCaseConfig {

    @Bean
    open fun startTwoFactorEnrollmentUseCase(userRepository: UserRepository) =
        StartTwoFactorEnrollmentUseCase(userRepository)

    @Bean
    open fun confirmTwoFactorEnrollmentUseCase(userRepository: UserRepository) =
        ConfirmTwoFactorEnrollmentUseCase(userRepository)

    @Bean
    open fun disableTwoFactorAuthenticationUseCase(userRepository: UserRepository) =
        DisableTwoFactorAuthenticationUseCase(userRepository)

    @Bean
    open fun adminDisableTwoFactorAuthenticationUseCase(userRepository: UserRepository) =
        AdminDisableTwoFactorAuthenticationUseCase(userRepository)
}
