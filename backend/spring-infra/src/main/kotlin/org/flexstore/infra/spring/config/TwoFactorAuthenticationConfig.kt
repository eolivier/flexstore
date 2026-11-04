package org.flexstore.infra.spring.config

import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.infra.repository.InMemoryTwoFactorChallengeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// A sign-in challenge is short-lived and does not need to survive a restart: kept in memory (see
// InMemoryTwoFactorChallengeRepository)
@Configuration
open class TwoFactorAuthenticationConfig {

    @Bean
    open fun twoFactorChallengeRepository(): TwoFactorChallengeRepository =
        InMemoryTwoFactorChallengeRepository()
}
