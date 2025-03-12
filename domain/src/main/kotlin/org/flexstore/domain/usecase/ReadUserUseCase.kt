package org.flexstore.domain.usecase

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.entity.UserRetrievalFailed
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class ReadUserUseCase(private val userRepository: UserRepository) : UseCase<User> {

    private lateinit var retrievedUser: User

    override fun getPreConditions(): List<PreCondition<User>> {
        println("#[BEGIN] ReadUserUseCase.getPreConditions")
        val userExistsCondition = PreCondition<User> { user ->
            assert(user.id.isValid()) { "User ID ${user.id.value} is invalid." }
        }
        val preConditions = listOf(userExistsCondition)
        println("#[END] ReadUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("#[BEGIN] ReadUserUseCase.getNominalScenario")
        // Steps
        val readUserStep = Step<User> { user -> retrievedUser = userRepository.findById(user.id) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(readUserStep))
        println("#[END] ReadUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        println("#[BEGIN] ReadUserUseCase.getPostConditions")
        val userRetrievedCondition = PostCondition<User> { user ->
            assert(retrievedUser is DefinedUser) { "User with ID ${user.id.value} does not exist." }
        }
        val postConditions = listOf(userRetrievedCondition)
        println("#[END] ReadUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<User>> {
        return mapOf(
            UserNotFoundException::class to AlternativeScenario(listOf()),
            UserRetrievalFailed::class to AlternativeScenario(listOf())
        )
    }

}
