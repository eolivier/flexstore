package org.flexstore.domain.user.administration

import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.user.administration.usecase.AccountStatusChange
import org.flexstore.domain.user.administration.usecase.ChangeAccountStatusUseCase
import org.flexstore.domain.user.administration.usecase.CreateUserRequest
import org.flexstore.domain.user.administration.usecase.CreateUserUseCase
import org.flexstore.domain.user.administration.usecase.DeleteUserUseCase
import org.flexstore.domain.user.administration.usecase.ListUsersUseCase
import org.flexstore.domain.user.administration.usecase.ReadUserUseCase
import org.flexstore.domain.user.administration.usecase.UpdateUserRequest
import org.flexstore.domain.user.administration.usecase.UpdateUserUseCase
import org.flexstore.domain.user.administration.usecase.UserDeletion
import org.flexstore.domain.user.administration.usecase.UserReading
import org.flexstore.domain.user.administration.usecase.UsersReading

class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(request: CreateUserRequest): User {
        val createUserUseCase = CreateUserUseCase(userRepository)
        createUserUseCase.unfold(request)
        return createUserUseCase.createdUser
    }

    fun readUser(reader: Actor, userId: UserId): User {
        val readUserUseCase = ReadUserUseCase(userRepository)
        readUserUseCase.unfold(UserReading(reader, userId))
        return readUserUseCase.retrievedUser
    }

    fun updateUser(request: UpdateUserRequest) {
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        updateUserUseCase.unfold(request)
    }

    fun changeAccountStatus(change: AccountStatusChange): User {
        ChangeAccountStatusUseCase(userRepository).unfold(change)
        return readUser(change.editor, change.userId)
    }

    fun deleteUser(editor: Actor, userId: UserId) {
        DeleteUserUseCase(userRepository).unfold(UserDeletion(editor, userId))
    }

    fun readAllUsers(reader: Actor, pageRequest: PageRequest): Page<Users> =
        ListUsersUseCase(userRepository).unfold(UsersReading(reader, pageRequest))

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        ChangePasswordUseCase(userRepository).unfold(changePasswordRequest)
    }
}