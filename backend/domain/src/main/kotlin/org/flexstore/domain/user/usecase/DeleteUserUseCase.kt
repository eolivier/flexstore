package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class DeleteUserUseCase(private val userRepository: UserRepository) : UseCase<UserId> {

    override fun getPreConditions(): List<PreCondition<UserId>> {
        println("#[BEGIN] DeleteUserUseCase.getPreConditions")
        val preConditions = listOf(userExistsCondition())
        println("#[END] DeleteUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<UserId> {
        println("#[BEGIN] DeleteUserUseCase.getNominalScenario")
        // Steps
        val deleteUserStep = Step<UserId> { userId -> userRepository.delete(userId) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(deleteUserStep))
        println("#[END] DeleteUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<UserId>> {
        println("#[BEGIN] DeleteUserUseCase.getPostConditions")
        val postConditions = listOf(userDeletedCondition())
        println("#[END] DeleteUserUseCase.getPostConditions")
        return postConditions
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