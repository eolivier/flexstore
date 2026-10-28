package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// The account of a user
class ReadAccountUseCase(private val userRepository: UserRepository) : QueryUseCase<UserId, Account> {

    override fun getPreConditions(): PreConditions<UserId> = PreConditions(userExistsCondition())

    override fun getNominalQuery() = NominalQuery<UserId, Account> { userId ->
        (userRepository.findById(userId) as User.DefinedUser).account()
    }

    override fun getPostConditions(): PostConditions<Account> = PostConditions.none()

    private fun userExistsCondition() = PreCondition<UserId> { userId ->
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }
}
