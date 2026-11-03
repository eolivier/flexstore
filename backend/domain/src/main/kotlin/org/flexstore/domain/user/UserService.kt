package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*

class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(request: CreateUserRequest): User {
        val createUserUseCase = CreateUserUseCase(userRepository)
        createUserUseCase.unfold(request)
        return createUserUseCase.createdUser
    }

    fun readUser(reader: UserId, userId: UserId): User {
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

    fun deleteUser(editor: UserId, userId: UserId) {
        DeleteUserUseCase(userRepository).unfold(UserDeletion(editor, userId))
    }

    fun readAllUsers(reader: UserId): Users = ListUsersUseCase(userRepository).unfold(UsersReading(reader))

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        ChangePasswordUseCase(userRepository).unfold(changePasswordRequest)
    }
}