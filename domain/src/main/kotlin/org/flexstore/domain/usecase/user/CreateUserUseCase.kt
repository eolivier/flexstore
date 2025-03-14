package org.flexstore.domain.usecase.user

import org.ucop.domain.NominalException
import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class CreateUserUseCase(private val userRepository: UserRepository) : UseCase<User> {

    override fun getPreConditions(): List<PreCondition<User>> {
        println("#[BEGIN] CreateUserUseCase.getPreConditions")
        val userDoesNotExistCondition = PreCondition<User> {
                user -> assert(userRepository.notExists(user.id)) { "User with ID ${user.id.value} already exists." }
        }
        val preConditions = listOf(userDoesNotExistCondition)
        println("#[END] CreateUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("#[BEGIN] CreateUserUseCase.getNominalScenario")
        // Steps
        val createUserStep = Step<User> { user -> userRepository.save(user) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(createUserStep))
        println("#[END] CreateUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        println("#[BEGIN] CreateUserUseCase.getPostConditions")
        val userExistsCondition = PostCondition<User> {
                user -> assert(userRepository.exists(user.id)) { "User with ID ${user.id.value} was not created." }
        }
        val postConditions = listOf(userExistsCondition)
        println("#[END] CreateUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<User>> {
        return mapOf(
            UserAlreadyExists::class to AlternativeScenario(listOf()),
            UserCreationFailed::class to AlternativeScenario(listOf())
        )
    }
}