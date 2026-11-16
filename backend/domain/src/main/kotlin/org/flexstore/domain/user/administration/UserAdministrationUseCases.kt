package org.flexstore.domain.user.administration

import org.flexstore.domain.user.administration.usecase.ChangeAccountStatusUseCase
import org.flexstore.domain.user.administration.usecase.CreateUserUseCase
import org.flexstore.domain.user.administration.usecase.DeleteUserUseCase
import org.flexstore.domain.user.administration.usecase.ListUsersUseCase
import org.flexstore.domain.user.administration.usecase.ReadUserUseCase
import org.flexstore.domain.user.administration.usecase.UpdateUserUseCase
import org.flexstore.domain.user.usecase.ChangePasswordUseCase

// The use cases of the administration of the users, and of the change of the password of a user
data class UserAdministrationUseCases(
    val createUserUseCase: CreateUserUseCase,
    val readUserUseCase: ReadUserUseCase,
    val updateUserUseCase: UpdateUserUseCase,
    val changeAccountStatusUseCase: ChangeAccountStatusUseCase,
    val deleteUserUseCase: DeleteUserUseCase,
    val listUsersUseCase: ListUsersUseCase,
    val changePasswordUseCase: ChangePasswordUseCase
)
