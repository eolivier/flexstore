package org.flexstore.domain.service

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.port.PasswordEncoder
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.user.*

class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(user: User): User {
        val createUserUseCase = CreateUserUseCase(userRepository, passwordEncoder)
        createUserUseCase.unfold(user)
        return createUserUseCase.createdUser
    }

    fun readUser(userId: UserId): User {
        val readUserUseCase = ReadUserUseCase(userRepository)
        readUserUseCase.unfold(userId)
        return readUserUseCase.retrievedUser
    }

    fun updateUser(user: User) {
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        updateUserUseCase.unfold(user)
    }

    fun deleteUser(userId: UserId) {
        DeleteUserUseCase(userRepository).unfold(userId)
    }

    fun readAllUsers(): List<User>  = userRepository.findAll()

    fun login(loginRequest: LoginRequest): User {
        val loginUseCase = LoginUseCase(userRepository, passwordEncoder)
        loginUseCase.unfold(loginRequest)
        return loginUseCase.authenticatedUser
    }
}