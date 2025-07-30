package org.flexstore.domain.service

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.user.CreateUserUseCase
import org.flexstore.domain.usecase.user.DeleteUserUseCase
import org.flexstore.domain.usecase.user.ReadUserUseCase
import org.flexstore.domain.usecase.user.UpdateUserUseCase

class UserService(private val userRepository: UserRepository) {

    fun createUser(user: User) {
        CreateUserUseCase(userRepository).unfold(user)
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
}