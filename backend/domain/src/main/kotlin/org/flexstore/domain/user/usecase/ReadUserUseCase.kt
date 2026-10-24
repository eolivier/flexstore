package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.User.DefinedUser
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class ReadUserUseCase(private val userRepository: UserRepository) : UseCase<UserId> {

    lateinit var retrievedUser: User

    override fun getPreConditions(): PreConditions<UserId> {
        return PreConditions(isValidUserIdCondition(), userExistsCondition())
    }

    override fun getNominalScenario(): NominalScenario<UserId> {
        // Steps
        val readUserStep = Step<UserId> { userId -> retrievedUser = userRepository.findById(userId) }
        // Nominal scenario
        return NominalScenario(listOf(readUserStep))
    }

    override fun getPostConditions(): PostConditions<UserId> {
        return PostConditions(userRetrievedCondition())
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserId>> {
        return emptyMap()
    }

    private fun userExistsCondition() = PreCondition<UserId> { userId ->
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }

    private fun isValidUserIdCondition() = PreCondition<UserId> { userId ->
        if (userId.isInvalid()) {
            throw InvalidUserIdException(ErrorMessage("User ID ${userId.value} is invalid."))
        }
    }

    private fun userRetrievedCondition() = PostCondition<UserId> { userId ->
        if (retrievedUser is User.UndefinedUser) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }
}
