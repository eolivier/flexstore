package org.flexstore.domain.usecase.user

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.entity.UserRetrievalFailed
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class ReadUserUseCase(private val userRepository: UserRepository) : UseCase<UserId> {

    lateinit var retrievedUser: User

    override fun getPreConditions(): List<PreCondition<UserId>> {
        println("#[BEGIN] ReadUserUseCase.getPreConditions")
        val isValidUserIdCondition = PreCondition<UserId> { userId ->
            assert(userId.isValid()) { "User ID ${userId.value} is invalid." }
        }
        val userExistsCondition = PreCondition<UserId> { userId ->
            assert(userRepository.exists(userId)) { "User with ID ${userId.value} does not exist." }
        }
        val preConditions = listOf(isValidUserIdCondition, userExistsCondition)
        println("#[END] ReadUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<UserId> {
        println("#[BEGIN] ReadUserUseCase.getNominalScenario")
        // Steps
        val readUserStep = Step<UserId> { userId -> retrievedUser = userRepository.findById(userId) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(readUserStep))
        println("#[END] ReadUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<UserId>> {
        println("#[BEGIN] ReadUserUseCase.getPostConditions")
        val userRetrievedCondition = PostCondition<UserId> { userId ->
            assert(retrievedUser is DefinedUser) { "User with ID ${userId.value} does not exist." }
        }
        val postConditions = listOf(userRetrievedCondition)
        println("#[END] ReadUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserId>> {
        return mapOf(
            UserNotFoundException::class to AlternativeScenario(listOf()),
            UserRetrievalFailed::class to AlternativeScenario(listOf())
        )
    }

}
