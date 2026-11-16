package org.flexstore.infra.spring.config

import org.flexstore.domain.user.administration.UserAdministrationUseCases
import org.flexstore.domain.user.administration.usecase.ChangeAccountStatusUseCase
import org.flexstore.domain.user.administration.usecase.CreateUserUseCase
import org.flexstore.domain.user.administration.usecase.DeleteUserUseCase
import org.flexstore.domain.user.administration.usecase.ListUsersUseCase
import org.flexstore.domain.user.administration.usecase.ReadUserUseCase
import org.flexstore.domain.user.administration.usecase.UpdateUserUseCase
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.usecase.ChangePasswordUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the administration of the users, declared step by step: use cases, then their group
@Configuration
open class UserAdministrationUseCaseConfig {

    @Bean
    open fun createUserUseCase(userRepository: UserRepository) = CreateUserUseCase(userRepository)

    @Bean
    open fun readUserUseCase(userRepository: UserRepository) = ReadUserUseCase(userRepository)

    @Bean
    open fun updateUserUseCase(userRepository: UserRepository) = UpdateUserUseCase(userRepository)

    @Bean
    open fun changeAccountStatusUseCase(userRepository: UserRepository) =
        ChangeAccountStatusUseCase(userRepository)

    @Bean
    open fun deleteUserUseCase(userRepository: UserRepository) = DeleteUserUseCase(userRepository)

    @Bean
    open fun listUsersUseCase(userRepository: UserRepository) = ListUsersUseCase(userRepository)

    @Bean
    open fun changePasswordUseCase(userRepository: UserRepository) = ChangePasswordUseCase(userRepository)

    @Bean
    open fun userAdministrationUseCases(
        createUserUseCase: CreateUserUseCase,
        readUserUseCase: ReadUserUseCase,
        updateUserUseCase: UpdateUserUseCase,
        changeAccountStatusUseCase: ChangeAccountStatusUseCase,
        deleteUserUseCase: DeleteUserUseCase,
        listUsersUseCase: ListUsersUseCase,
        changePasswordUseCase: ChangePasswordUseCase
    ) = UserAdministrationUseCases(
        createUserUseCase, readUserUseCase, updateUserUseCase, changeAccountStatusUseCase, deleteUserUseCase,
        listUsersUseCase, changePasswordUseCase
    )
}
