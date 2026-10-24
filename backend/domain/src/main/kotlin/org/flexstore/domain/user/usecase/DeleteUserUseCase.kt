package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class DeleteUserUseCase(private val userRepository: UserRepository) : UseCase<UserId> {

    override fun getPreConditions(): PreConditions<UserId> {
        return PreConditions(userExistsCondition())
    }

    override fun getNominalScenario(): NominalScenario<UserId> {
        // Steps
        val deleteUserStep = Step<UserId> { userId -> userRepository.delete(userId) }
        // Nominal scenario
        return NominalScenario(listOf(deleteUserStep))
    }

    override fun getPostConditions(): PostConditions<UserId> {
        return PostConditions(userDeletedCondition())
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserId>> {
        return emptyMap()
    }

    private fun userExistsCondition() = PreCondition<UserId> { userId ->
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }

    private fun userDeletedCondition() = PostCondition<UserId> { userId ->
        if (userRepository.exists(userId)) {
            throw UserDeletionFailed(ErrorMessage("User with ID ${userId.value} was not deleted."))
        }
    }
}