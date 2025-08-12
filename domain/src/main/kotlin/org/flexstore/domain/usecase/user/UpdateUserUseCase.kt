package org.flexstore.domain.usecase.user

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*

class UpdateUserUseCase(private val userRepository: UserRepository) : UseCase<User> {

    override fun getPreConditions(): List<PreCondition<User>> {
        println("#[BEGIN] UpdateUserUseCase.getPreConditions")
        val preConditions = listOf(isValidUserIdCondition(), userDoExistCondition())
        println("#[END] UpdateUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("#[BEGIN] UpdateUserUseCase.getNominalScenario")
        // Steps
        val updateUserStep = Step<User> { user -> userRepository.save(user) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(updateUserStep))
        println("#[END] UpdateUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        println("#[BEGIN] UpdateUserUseCase.getPostConditions")
        val postConditions = listOf<EmptyPostCondition<User>>(EmptyPostCondition())
        println("#[END] UpdateUserUseCase.getPostConditions")
        return postConditions
    }

    private fun isValidUserIdCondition() = PreCondition<User> { user ->
        if (user.id.isInvalid()) {
            throw InvalidUserIdException(NonEmptyString("User ID ${user.id.value} is invalid."))
        }
    }

    private fun userDoExistCondition() = PreCondition<User> { user ->
        if (userRepository.notExists(user.id)) {
            throw UserNotFoundException(NonEmptyString("User with ID ${user.id.value} does not exist."))
        }
    }
}