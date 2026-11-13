package org.flexstore.infra.spring.config

import org.flexstore.domain.cookie.CookieConsentRepository
import org.flexstore.domain.cookie.usecase.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the cookie consent sub-domain, declared step by step: use cases, then their group
@Configuration
open class CookieConsentUseCaseConfig {

    @Bean
    open fun readCookieConsentUseCase(
        cookieConsentRepository: CookieConsentRepository,
        userRepository: UserRepository
    ) = ReadCookieConsentUseCase(cookieConsentRepository, userRepository)

    @Bean
    open fun recordCookieConsentUseCase(
        cookieConsentRepository: CookieConsentRepository,
        userRepository: UserRepository
    ) = RecordCookieConsentUseCase(cookieConsentRepository, userRepository)

    @Bean
    open fun cookieConsentUseCases(
        readCookieConsentUseCase: ReadCookieConsentUseCase,
        recordCookieConsentUseCase: RecordCookieConsentUseCase
    ) = CookieConsentUseCases(readCookieConsentUseCase, recordCookieConsentUseCase)
}
