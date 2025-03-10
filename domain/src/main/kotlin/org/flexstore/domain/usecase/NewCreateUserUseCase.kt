package org.flexstore.domain.usecase

import org.flexstore.domain.NominalException
import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository
import kotlin.reflect.KClass

class NewCreateUserUseCase(private val userRepository: UserRepository) : NewUseCase<User> {

    override fun getPreConditions(): List<PreCondition<User>> {
        val userDoesNotExistCondition = PreCondition<User> {
                user -> assert(userRepository.notExists(user.id)) { "User with ID ${user.id.value} already exists." }
        }
        return listOf(userDoesNotExistCondition)
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        val userExistsCondition = PostCondition<User> {
                user -> assert(userRepository.exists(user.id)) { "User with ID ${user.id.value} was not created." }
        }
        return listOf(userExistsCondition)
    }

    override fun getNominalScenario(): NominalScenario<User> {
        // Steps
        val createUserStep = Step<User> { user -> userRepository.save(user) }
        // Nominal scenario
        return NominalScenario(listOf(createUserStep))
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<User>> {
        return mapOf(
            UserAlreadyExists::class to AlternativeScenario(listOf()),
            UserCreationFailed::class to AlternativeScenario(listOf())
        )
    }
}