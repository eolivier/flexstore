package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.ucop.domain.UseCaseLog

class ReadAccountUseCase(private val userRepository: UserRepository) {

    fun getAccount(userId: UserId): User.DefinedUser = UseCaseLog.execution("ReadAccountUseCase.getAccount") {
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
        userRepository.findById(userId) as User.DefinedUser
    }
}
