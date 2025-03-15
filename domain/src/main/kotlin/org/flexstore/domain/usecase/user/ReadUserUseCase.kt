package org.flexstore.domain.usecase.user

import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class ReadUserUseCase(private val userRepository: UserRepository) : UseCase<UserId> {

    lateinit var retrievedUser: User

    override fun getPreConditions(): List<PreCondition<UserId>> {
        println("#[BEGIN] ReadUserUseCase.getPreConditions")
        val isValidUserIdCondition = PreCondition<UserId> { userId ->
            assert(userId.isValid()) { throw InvalidUserIdException(NonEmptyString("User ID ${userId.value} is invalid.")) }
        }
        val userExistsCondition = PreCondition<UserId> { userId ->
            assert(userRepository.exists(userId)) { throw UserNotFoundException(NonEmptyString("User with ID ${userId.value} does not exist.")) }
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
            assert(retrievedUser is DefinedUser) { throw UserNotFoundException(NonEmptyString("User with ID ${userId.value} does not exist.")) }
        }
        val postConditions = listOf(userRetrievedCondition)
        println("#[END] ReadUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserId>> {
        return emptyMap()
    }

}
