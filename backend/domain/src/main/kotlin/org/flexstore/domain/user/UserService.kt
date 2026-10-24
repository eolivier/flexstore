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

    fun readUser(userId: UserId): User {
        val readUserUseCase = ReadUserUseCase(userRepository)
        readUserUseCase.unfold(userId)
        return readUserUseCase.retrievedUser
    }

    fun updateUser(request: UpdateUserRequest) {
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        updateUserUseCase.unfold(request)
    }

    fun changeAccountStatus(change: AccountStatusChange): User {
        ChangeAccountStatusUseCase(userRepository).unfold(change)
        return readUser(change.userId)
    }

    fun deleteUser(userId: UserId) {
        DeleteUserUseCase(userRepository).unfold(userId)
    }

    fun readAllUsers(): List<User>  = userRepository.findAll()

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        ChangePasswordUseCase(userRepository).unfold(changePasswordRequest)
    }

    fun login(loginRequest: LoginRequest): User {
        val loginUseCase = LoginUseCase(userRepository)
        loginUseCase.unfold(loginRequest)
        return loginUseCase.authenticatedUser
    }
}