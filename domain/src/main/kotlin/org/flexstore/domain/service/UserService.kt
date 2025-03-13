package org.flexstore.domain.service

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.CreateUserUseCase
import org.flexstore.domain.usecase.DeleteUserUseCase
import org.flexstore.domain.usecase.ReadUserUseCase
import org.flexstore.domain.usecase.UpdateUserUseCase

class UserService(private val userRepository: UserRepository) {

    fun createUser(user: User) {
        CreateUserUseCase(userRepository).run(user)
    }

    fun readUser(userId: UserId): User {
        val readUserUseCase = ReadUserUseCase(userRepository)
        readUserUseCase.run(userId)
        return readUserUseCase.retrievedUser
    }

    fun updateUser(user: User) {
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        updateUserUseCase.run(user)
    }

    fun deleteUser(userId: UserId) {
        DeleteUserUseCase(userRepository).run(userId)
    }
}