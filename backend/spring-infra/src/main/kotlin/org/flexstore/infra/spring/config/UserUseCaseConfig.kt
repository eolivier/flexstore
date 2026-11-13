package org.flexstore.infra.spring.config

import org.flexstore.domain.actor.*
import org.flexstore.domain.email.*
import org.flexstore.domain.email.model.*
import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.user.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the user sub-domain, declared step by step: service, use cases, then their group
@Configuration
open class UserUseCaseConfig(private val shopUrl: ShopUrl) {

    @Bean
    open fun actorResolutionService(userRepository: UserRepository) = ActorResolutionService(userRepository)

    @Bean
    open fun registerCustomerUseCase(userRepository: UserRepository): RegisterCustomerUseCase {
        return RegisterCustomerUseCase(userRepository)
    }

    @Bean
    // The links of the verification emails open the page /verify-email of the front
    open fun sendVerificationEmailUseCase(userRepository: UserRepository,
                                          emailRepository: EmailRepository): SendVerificationEmailUseCase {
        val verificationPage = VerificationPage.of(shopUrl)
        return SendVerificationEmailUseCase(userRepository, emailRepository, verificationPage)
    }

    @Bean
    open fun verifyEmailUseCase(userRepository: UserRepository,
                                emailRepository: EmailRepository): VerifyEmailUseCase {
        return VerifyEmailUseCase(userRepository, emailRepository)
    }

    @Bean
    open fun requestPasswordResetUseCase(userRepository: UserRepository,
                                         passwordResetRepository: PasswordResetRepository,
                                         emailRepository: EmailRepository) =
        RequestPasswordResetUseCase(userRepository, passwordResetRepository, emailRepository)

    @Bean
    open fun resetPasswordUseCase(userRepository: UserRepository,
                                  passwordResetRepository: PasswordResetRepository) =
        ResetPasswordUseCase(userRepository, passwordResetRepository)

    @Bean
    open fun readAccountUseCase(userRepository: UserRepository): ReadAccountUseCase {
        return ReadAccountUseCase(userRepository)
    }

    @Bean
    open fun recordFailedSignInUseCase(userRepository: UserRepository,
                                       failedSignInRepository: FailedSignInRepository) =
        RecordFailedSignInUseCase(userRepository, failedSignInRepository)

    @Bean
    open fun resetFailedSignInsUseCase(failedSignInRepository: FailedSignInRepository) =
        ResetFailedSignInsUseCase(failedSignInRepository)
}
