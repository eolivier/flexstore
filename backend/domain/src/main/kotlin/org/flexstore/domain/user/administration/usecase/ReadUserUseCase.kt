package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator who reads a user
data class UserReading(val reader: Actor, val userId: UserId)

class ReadUserUseCase(private val userRepository: UserRepository) : QueryUseCase<UserReading, User> {

    override fun getPreConditions(): PreConditions<UserReading> {
        return PreConditions(readerIsAdministratorCondition(), isValidUserIdCondition(), userExistsCondition())
    }

    override fun getNominalQuery() = NominalQuery<UserReading, User> { (_, userId) ->
        userRepository.findById(userId)
    }

    override fun getPostConditions(): PostConditions<User> = PostConditions(userRetrievedCondition())

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<UserReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            throw UserReadingForbiddenException(ErrorMessage("Only an administrator can read a user."))
        }
    }

    private fun userExistsCondition() = PreCondition<UserReading> { (_, userId) ->
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }

    private fun isValidUserIdCondition() = PreCondition<UserReading> { (_, userId) ->
        if (userId.isInvalid()) {
            throw InvalidUserIdException(ErrorMessage("User ID ${userId.value} is invalid."))
        }
    }

    private fun userRetrievedCondition() = PostCondition<User> { retrievedUser ->
        if (retrievedUser is User.UndefinedUser) {
            throw UserRetrievalException(ErrorMessage("User with ID ${retrievedUser.id.value} does not exist."))
        }
    }
}
