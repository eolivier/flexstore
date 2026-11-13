package org.flexstore.infra.spring.config

import org.flexstore.domain.user.address.AddressUseCases
import org.flexstore.domain.user.address.usecase.AddPostalAddressUseCase
import org.flexstore.domain.user.address.usecase.ChangePostalAddressUseCase
import org.flexstore.domain.user.address.usecase.RemovePostalAddressUseCase
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the address sub-domain, declared step by step: use cases, then their group
@Configuration
open class AddressUseCaseConfig {

    @Bean
    open fun addPostalAddressUseCase(userRepository: UserRepository) =
        AddPostalAddressUseCase(userRepository)

    @Bean
    open fun changePostalAddressUseCase(userRepository: UserRepository) =
        ChangePostalAddressUseCase(userRepository)

    @Bean
    open fun removePostalAddressUseCase(userRepository: UserRepository) =
        RemovePostalAddressUseCase(userRepository)

    @Bean
    open fun addressUseCases(addPostalAddressUseCase: AddPostalAddressUseCase,
                             changePostalAddressUseCase: ChangePostalAddressUseCase,
                             removePostalAddressUseCase: RemovePostalAddressUseCase) =
        AddressUseCases(addPostalAddressUseCase, changePostalAddressUseCase, removePostalAddressUseCase)
}
