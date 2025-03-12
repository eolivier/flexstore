package org.flexstore.domain.usecase

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.entity.UserDeletionFailed
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class DeleteUserUseCase(private val userRepository: UserRepository) : UseCase<User> {

    override fun getPreConditions(): List<PreCondition<User>> {
        println("#[BEGIN] DeleteUserUseCase.getPreConditions")
        val userExistsCondition = PreCondition<User> { user ->
            assert(userRepository.exists(user.id)) { "User with ID ${user.id.value} does not exist." }
        }
        val preConditions = listOf(userExistsCondition)
        println("#[END] DeleteUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("#[BEGIN] DeleteUserUseCase.getNominalScenario")
        // Steps
        val deleteUserStep = Step<User> { user -> userRepository.delete(user.id) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(deleteUserStep))
        println("#[END] DeleteUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        println("#[BEGIN] DeleteUserUseCase.getPostConditions")
        val userDeletedCondition = PostCondition<User> { user ->
            assert(userRepository.notExists(user.id)) { "User with ID ${user.id.value} was not deleted." }
        }
        val postConditions = listOf(userDeletedCondition)
        println("#[END] DeleteUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<User>> {
        return mapOf(
            UserNotFoundException::class to AlternativeScenario(listOf()),
            UserDeletionFailed::class to AlternativeScenario(listOf())
        )
    }
}