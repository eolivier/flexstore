package org.flexstore.domain.usecase.user

import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.entity.UserDeletionFailed
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class DeleteUserUseCase(private val userRepository: UserRepository) : UseCase<UserId> {

    override fun getPreConditions(): List<PreCondition<UserId>> {
        println("#[BEGIN] DeleteUserUseCase.getPreConditions")
        val userExistsCondition = PreCondition<UserId> { userId ->
            assert(userRepository.exists(userId)) { throw UserNotFoundException(NonEmptyString("User with ID ${userId.value} does not exist.")) }
        }
        val preConditions = listOf(userExistsCondition)
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
        val userDeletedCondition = PostCondition<UserId> { userId ->
            assert(userRepository.notExists(userId)) { throw UserDeletionFailed(NonEmptyString("User with ID ${userId.value} was not deleted.")) }
        }
        val postConditions = listOf(userDeletedCondition)
        println("#[END] DeleteUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserId>> {
        return emptyMap()
    }
}