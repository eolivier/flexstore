package org.flexstore.infra.spring.config

import org.flexstore.domain.legal.*
import org.flexstore.domain.legal.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the legal page sub-domain, declared step by step: use cases, then their group
@Configuration
open class LegalPageUseCaseConfig {

    @Bean
    open fun readLegalPageUseCase(legalPageRepository: LegalPageRepository) =
        ReadLegalPageUseCase(legalPageRepository)

    @Bean
    open fun publishLegalPageUseCase(legalPageRepository: LegalPageRepository) =
        PublishLegalPageUseCase(legalPageRepository)

    @Bean
    open fun legalPageUseCases(readLegalPageUseCase: ReadLegalPageUseCase,
                               publishLegalPageUseCase: PublishLegalPageUseCase) =
        LegalPageUseCases(readLegalPageUseCase, publishLegalPageUseCase)
}
