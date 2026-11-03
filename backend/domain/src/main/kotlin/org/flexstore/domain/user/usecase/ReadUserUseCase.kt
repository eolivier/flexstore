package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

// An administrator who reads a user
data class UserReading(val reader: UserId, val userId: UserId)

class ReadUserUseCase(private val userRepository: UserRepository) : UseCase<UserReading> {

    lateinit var retrievedUser: User

    override fun getPreConditions(): PreConditions<UserReading> {
        return PreConditions(readerIsAdministratorCondition(), isValidUserIdCondition(), userExistsCondition())
    }

    override fun getNominalScenario(): NominalScenario<UserReading> {
        // Steps
        val readUserStep = Step<UserReading> { (_, userId) -> retrievedUser = userRepository.findById(userId) }
        // Nominal scenario
        return NominalScenario(listOf(readUserStep))
    }

    override fun getPostConditions(): PostConditions<UserReading> {
        return PostConditions(userRetrievedCondition())
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserReading>> {
        return emptyMap()
    }

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<UserReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
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

    private fun userRetrievedCondition() = PostCondition<UserReading> { (_, userId) ->
        if (retrievedUser is User.UndefinedUser) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }
}
